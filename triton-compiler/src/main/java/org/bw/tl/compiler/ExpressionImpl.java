package org.bw.tl.compiler;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.resolve.ExpressionResolverImpl;
import org.bw.tl.compiler.resolve.FieldContext;
import org.bw.tl.compiler.resolve.Operator;
import org.bw.tl.compiler.resolve.SymbolContext;
import org.bw.tl.compiler.types.AnyTypeHandler;
import org.bw.tl.compiler.types.TypeHandler;
import org.bw.tl.util.TypeUtilities;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bw.tl.util.TypeUtilities.*;
import static org.bw.tl.util.TypeUtilities.isAssignableWithImplicitCast;

@EqualsAndHashCode(callSuper = true)
public @Data class ExpressionImpl extends ASTVisitorBase implements Opcodes {

    private final MethodVisitor mv;
    private final MethodCtx ctx;

    @Override
    public void visitCall(final Call call) {
        final SymbolContext funCtx = ctx.getResolver().resolveCallCtx(call);

        if (funCtx != null) {
            final List<Expression> expressions = call.getParameters();
            final Type[] argumentTypes = funCtx.getTypeDescriptor().getArgumentTypes();

            if (!funCtx.isStatic()) {
                call.getPrecedingExpr().accept(this);
            }

            for (int i = 0; i < expressions.size(); i++) {
                expressions.get(i).accept(this);
                final Type exprType = expressions.get(i).resolveType(ctx.getResolver());

                if (isAssignableWithImplicitCast(exprType, argumentTypes[i])) {
                    final TypeHandler from = getTypeHandler(exprType);
                    final TypeHandler to = getTypeHandler(argumentTypes[i]);
                    to.cast(mv, from);
                }
            }

            boolean itf = isInterface(funCtx.getOwner());
            int opcode = funCtx.isStatic() ? INVOKESTATIC : INVOKEVIRTUAL;

            if (itf)
                opcode = INVOKEINTERFACE;

            mv.visitMethodInsn(opcode, funCtx.getOwner(), funCtx.getName(), funCtx.getTypeDescriptor().getDescriptor(), itf);

            if (call.shouldPop() && !funCtx.getTypeDescriptor().getReturnType().equals(Type.VOID_TYPE)) {
                final Type retType = funCtx.getTypeDescriptor().getReturnType();
                if (retType.equals(Type.LONG_TYPE) || retType.equals(Type.DOUBLE_TYPE)) {
                    mv.visitInsn(DUP2);
                } else {
                    mv.visitInsn(DUP);
                }
            }
        } else {
            ctx.reportError("Cannot resolve function", call);
        }
    }

    @Override
    public void visitNew(final New newExpr) {
        final List<Expression> parameters = newExpr.getParameters();
        final boolean isArray = newExpr.isArray();

        if (isArray) {
            final Type componentType = ctx.getResolver().resolveType(newExpr.getType());

            if (componentType == null) {
                ctx.reportError("Cannot resolve type: " + newExpr.getType(), newExpr);
            } else {
                final TypeHandler handler = getTypeHandler(componentType);

                for (final Expression parameter : parameters) {
                    final Type parameterType = parameter.resolveType(ctx.getResolver());
                    if (!isAssignableFrom(parameterType, Type.INT_TYPE)) {
                        ctx.reportError("Expected type: int, but got: " + parameterType.getClassName(), parameter);
                        return;
                    }
                    parameter.accept(this);
                }

                if (parameters.isEmpty()) {
                    ctx.reportError("Cannot instantiate 0 dimensional array", newExpr);
                } else if (parameters.size() == 1) {
                    handler.newArray(mv);
                } else {
                    handler.multiNewArray(mv, parameters.size());
                }
                if (newExpr.shouldPop()) {
                    ctx.reportError("Not a statement", newExpr);
                }
            }

        } else {
            final SymbolContext constructorCtx = ctx.getResolver().resolveConstructorCtx(newExpr);

            if (constructorCtx == null) {
                ctx.reportError("Cannot resolve constructor", newExpr);
                return;
            }

            mv.visitTypeInsn(NEW, constructorCtx.getOwner());

            if (!newExpr.shouldPop())
                mv.visitInsn(DUP);

            final Type[] constructorArgTypes = constructorCtx.getTypeDescriptor().getArgumentTypes();
            for (int i = 0; i < constructorArgTypes.length; i++) {
                final TypeHandler to = getTypeHandler(constructorArgTypes[i]);
                final Type paramType = parameters.get(i).resolveType(ctx.getResolver());
                final TypeHandler from = getTypeHandler(paramType);

                parameters.get(i).accept(this);
                to.cast(mv, from);
            }

            mv.visitMethodInsn(INVOKESPECIAL, constructorCtx.getOwner(), constructorCtx.getName(),
                    constructorCtx.getTypeDescriptor().getDescriptor(), false);
        }
    }

    @Override
    public void visitAssignment(final Assignment assignment) {
        final Expression precedingExpr = assignment.getPrecedingExpr();
        final Type valueType = assignment.resolveType(ctx.getResolver());


        if (valueType == null) {
            ctx.reportError("Cannot resolve expression", assignment.getValue());
            return;
        }

        final TypeHandler from = getTypeHandler(valueType);

        final FieldContext fieldCtx = ctx.getResolver().resolveFieldCtx(precedingExpr, assignment.getName());

        if (fieldCtx == null) {
            ctx.reportError("Cannot resolve field: " + assignment.getName(), assignment);
            return;
        }

        if (fieldCtx.isFinal() && !ctx.isInitializer()) {
            ctx.reportError("Cannot assign value to final field", assignment);
            return;
        }

        if (precedingExpr != null && !fieldCtx.isStatic())
            precedingExpr.accept(this);

        final TypeHandler to = getTypeHandler(fieldCtx.getTypeDescriptor());

        assignment.getValue().accept(this);

        if (!assignment.shouldPop()) {
            if (valueType.equals(Type.LONG_TYPE) || valueType.equals(Type.DOUBLE_TYPE)) {
                mv.visitInsn(DUP2);
            } else {
                mv.visitInsn(DUP);
            }
        }

        if (!fieldCtx.getTypeDescriptor().equals(valueType) && !isAssignableFrom(valueType, fieldCtx.getTypeDescriptor())) {
            if (isAssignableWithImplicitCast(valueType, fieldCtx.getTypeDescriptor())) {
                to.cast(mv, from);
            } else {
                ctx.reportError("Expected type: " + fieldCtx.getTypeDescriptor().getClassName() + " but got: " +
                        valueType.getClassName(), assignment.getValue());
            }
        }

        if (fieldCtx == ExpressionResolverImpl.ARRAY_LENGTH) {
            ctx.reportError("Cannot modify array length", assignment);
        } else if (fieldCtx.isLocal()) {
            to.store(mv, ctx.getScope().findVar(fieldCtx.getName()).getIndex());
        } else if (fieldCtx.isStatic()) {
            mv.visitFieldInsn(PUTSTATIC, fieldCtx.getOwner(), fieldCtx.getName(), fieldCtx.getTypeDescriptor().getDescriptor());
        } else {
            if (!ctx.isStatic() && assignment.getPrecedingExpr() == null) {
                mv.visitVarInsn(ALOAD, 0); // put this on stack
            } else if (assignment.getPrecedingExpr() != null) {
                assignment.getPrecedingExpr().accept(this);
            } else if (ctx.isStatic()) {
                ctx.reportError("Cannot access non static field: " + assignment.getName() + " from static context",
                        assignment);
            }

            mv.visitFieldInsn(PUTFIELD, fieldCtx.getOwner(), fieldCtx.getName(), fieldCtx.getTypeDescriptor().getDescriptor());
        }
    }

    private void andOperator(final Expression lhs, final Expression rhs, final Type leftType, final Type rightType) {
        if (!leftType.equals(Type.BOOLEAN_TYPE)) {
            ctx.reportError("Expected type: boolean but got: " + rightType.getClassName(), lhs);
        }

        if (!rightType.equals(Type.BOOLEAN_TYPE)) {
            ctx.reportError("Expected type: boolean but got: " + rightType.getClassName(), rhs);
        }

        final Label trueLabel = new Label();
        final Label falseLabel = new Label();

        lhs.accept(this);
        mv.visitJumpInsn(IFEQ, falseLabel);

        rhs.accept(this);
        mv.visitJumpInsn(IFEQ, falseLabel);

        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, trueLabel);

        mv.visitLabel(falseLabel);
        mv.visitInsn(ICONST_0);

        mv.visitLabel(trueLabel);
    }

    private void orOperator(final Expression lhs, final Expression rhs, final Type leftType, final Type rightType) {
        if (!leftType.equals(Type.BOOLEAN_TYPE)) {
            ctx.reportError("Expected type: boolean but got: " + rightType.getClassName(), lhs);
        }

        if (!rightType.equals(Type.BOOLEAN_TYPE)) {
            ctx.reportError("Expected type: boolean but got: " + rightType.getClassName(), rhs);
        }

        final Label trueLabel = new Label();
        final Label endLabel = new Label();
        final Label falseLabel = new Label();

        lhs.accept(this);
        mv.visitJumpInsn(IFNE, trueLabel);

        rhs.accept(this);
        mv.visitJumpInsn(IFNE, trueLabel);

        mv.visitJumpInsn(GOTO, falseLabel);

        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, endLabel);

        mv.visitLabel(falseLabel);
        mv.visitInsn(ICONST_0);

        mv.visitLabel(endLabel);
    }

    private int depthCounter = 0;

    private void stringAdd(final Expression lhs, final Expression rhs, final Type leftType, final Type rightType) {
        if (depthCounter == 0) {
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        }

        depthCounter++;

        append(lhs, leftType);
        append(rhs, rightType);

        depthCounter--;

        if (depthCounter == 0) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        }
    }

    private void append(final Expression expr, final Type type) {
        Type typeToAppend = type;

        if (!Type.getType(String.class).equals(type) && isAssignableFrom(type, Type.getType(Object.class))) {
            typeToAppend = Type.getType(Object.class);
        } else if (isAssignableFrom(type, Type.INT_TYPE)) {
            typeToAppend = Type.INT_TYPE;
        }

        final Type appendDesc = Type.getMethodType(Type.getType(StringBuilder.class), typeToAppend);

        expr.accept(this);

        if (!(expr instanceof BinaryOp) || !Type.getType(String.class).equals(type)) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", appendDesc.getDescriptor(), false);
        }
    }

    @Override
    public void visitBinaryOp(final BinaryOp binaryOp) {
        final Expression lhs = binaryOp.getLeftSide();
        final Expression rhs = binaryOp.getRightSide();
        final Type leftType = lhs.resolveType(ctx.getResolver());
        final Type rightType = rhs.resolveType(ctx.getResolver());

        if (leftType == null) {
            ctx.reportError("Cannot resolve expression", binaryOp.getLeftSide());
            return;
        }

        if (rightType == null) {
            ctx.reportError("Cannot resolve expression", binaryOp.getRightSide());
            return;
        }

        final Type stringType = Type.getType(String.class);

        if (binaryOp.getOperator().equals("&&")) {
            andOperator(lhs, rhs, leftType, rightType);
            return;
        } else if (binaryOp.getOperator().equals("||")) {
            orOperator(lhs, rhs, leftType, rightType);
            return;
        } else if ((isAssignableFrom(leftType, stringType) || isAssignableFrom(rightType, stringType)) &&
                "+".equals(binaryOp.getOperator())) {
            stringAdd(lhs, rhs, leftType, rightType);
            return;
        }

        final Operator op = Operator.getOperator(binaryOp.getOperator(), leftType, rightType);

        if (op == null) {
            ctx.reportError("No such operator (" + leftType.getClassName() + " " + binaryOp.getOperator() +
                    " " + rightType.getClassName() + ")", binaryOp);
        } else {

            lhs.accept(this);

            if (!op.getLhs().equals(op.getResultType()) && isAssignableWithImplicitCast(op.getLhs(), op.getRhs())) {
                final TypeHandler to = getTypeHandler(rightType);
                final TypeHandler from = getTypeHandler(leftType);
                to.cast(mv, from);
                // lhs must be cast
            }

            rhs.accept(this);

            if (!op.getRhs().equals(op.getResultType()) && isAssignableWithImplicitCast(op.getRhs(), op.getLhs())) {
                final TypeHandler to = getTypeHandler(leftType);
                final TypeHandler from = getTypeHandler(rightType);
                to.cast(mv, from);
                // rhs must be cast
            }

            op.apply(mv);

            if (binaryOp.shouldPop()) {
                final Type opType = op.getResultType();
                if (opType.equals(Type.LONG_TYPE) || opType.equals(Type.DOUBLE_TYPE)) {
                    mv.visitInsn(DUP2);
                } else {
                    mv.visitInsn(DUP);
                }
            }
        }
    }

    @Override
    public void visitLiteral(final Literal literal) {
        final Type type = literal.resolveType(ctx.getResolver());

        if (literal.getValue() == null) {
            mv.visitInsn(ACONST_NULL);
        } else if (type != null) {
            final Object value = literal.getValue();
            if (value instanceof Long) {
                long val = (Long) value;
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    pushInteger(((Long) value).intValue());
                } else {
                    mv.visitLdcInsn(value);
                }
            } else if (value instanceof Float) {
                pushFloat((Float) value);
            } else if (value instanceof Double) {
                pushDouble((Double) value);
            } else if (value instanceof Boolean) {
                pushInteger((Boolean) value ? 1 : 0);
            } else if (value instanceof String) {
                pushString(literal);
            } else {
                mv.visitLdcInsn(value);
            }
        } else {
            System.err.println("Invalid literal type");
        }
    }

    private void pushString(final Literal<String> value) {
        final String str = value.getValue();
        final Pattern identifierPattern = Pattern.compile("\\$[a-zA-Z_][a-zA-Z_0-9]*");
        final Matcher matcher = identifierPattern.matcher(str);
        final List<Expression> expressions = new ArrayList<>();

        int index = 0;

        while (matcher.find()) {
            final int end = matcher.end();
            final int start = matcher.start();
            final Literal<String> lhs = new Literal<>(str.substring(index, start));

            lhs.setText(str.substring(index, start));
            lhs.setLineNumber(value.getLineNumber());
            lhs.setFile(value.getFile());
            lhs.setParent(value);

            if (start - index > 0) {
                expressions.add(lhs);
            }

            final QualifiedName id = QualifiedName.of(str.substring(matcher.start() + 1, end));
            id.setText(str.substring(matcher.start(), end));
            id.setLineNumber(value.getLineNumber());
            id.setFile(value.getFile());
            id.setParent(value);

            expressions.add(id);

            index = end;
        }

        if (index < str.length()) {
            final Literal<String> id = new Literal<>(str.substring(index));

            id.setLineNumber(value.getLineNumber());
            id.setText(str.substring(index));
            id.setFile(value.getFile());
            id.setParent(value);

            expressions.add(id);
        }

        if (expressions.size() < 2) {
            mv.visitLdcInsn(str);
        } else {
            final Expression bop = addExpressions(expressions);
            bop.accept(this);
        }
    }

    private Expression addExpressions(final List<Expression> expressions) {
        final int len = expressions.size();

        if (len == 2) {
            return new BinaryOp(expressions.get(0), "+", expressions.get(1));
        } else if (len == 1) {
            return expressions.get(0);
        }

        final Expression lhs = addExpressions(expressions.subList(0, len / 2));
        final Expression rhs = addExpressions(expressions.subList(len / 2, len));

        return addExpressions(Arrays.asList(lhs, rhs));
    }

    @Override
    public void visitExpressionIndices(final ExpressionIndex expressionIndex) {
        final Type resultType = expressionIndex.resolveType(ctx.getResolver());
        final Expression value = expressionIndex.getValue();

        if (resultType == null) {
            ctx.reportError("Cannot resolve expression", expressionIndex);
        } else {
            if (value != null) {
                visitAssignIdx(expressionIndex.getExpression(), resultType, expressionIndex.getIndices(), value,
                        !expressionIndex.shouldPop());
            } else {
                if (expressionIndex.shouldPop()) {
                    ctx.reportError("Not a statement", expressionIndex);
                } else {
                    visitIndex(expressionIndex.getExpression(), expressionIndex.getIndices());
                }
            }
        }
    }

    private void visitAssignIdx(final Expression array, final Type resultType, final List<Expression> indices,
                                final Expression value, final boolean duplicate) {
        final Type valueType = value.resolveType(ctx.getResolver());

        if (!valueType.equals(resultType) && !isAssignableFrom(valueType, resultType)) {
            ctx.reportError("Expected type: " + resultType.getClassName() + " but got: " + valueType.getClassName(), value);
            return;
        }

        array.accept(this);

        final TypeHandler arrayHandler = new AnyTypeHandler("LJava/lang/Object;");

        for (int i = 0; i + 1 < indices.size(); i++) {
            final Expression index = indices.get(i);
            final Type indexType = index.resolveType(ctx.getResolver());

            if (indexType == null) {
                ctx.reportError("Cannot resolve expression", index);
            } else if (!isAssignableFrom(indexType, Type.INT_TYPE)) {
                ctx.reportError("Expected type: int, but got: " + indexType.getClassName(), index);
            }

            index.accept(this);
            arrayHandler.arrayLoad(mv);
        }

        final Expression lastIndex = indices.get(indices.size() - 1);
        final Type lastIndexType = lastIndex.resolveType(ctx.getResolver());

        if (lastIndexType == null) {
            ctx.reportError("Cannot resolve expression", lastIndex);
        } else if (!isAssignableFrom(lastIndexType, Type.INT_TYPE)) {
            ctx.reportError("Expected type: int, but got: " + lastIndexType.getClassName(), lastIndex);
        } else {
            lastIndex.accept(this);

            value.accept(this);

            if (duplicate) {
                if (valueType.equals(Type.LONG_TYPE) || valueType.equals(Type.DOUBLE_TYPE)) {
                    mv.visitInsn(DUP2);
                } else {
                    mv.visitInsn(DUP);
                }
            }

            if (getDim(resultType) > indices.size()) {
                arrayHandler.arrayStore(mv);
            } else {
                getTypeHandler(resultType).arrayStore(mv);
            }
        }
    }

    private void visitIndex(final Expression lstArrayOrMap, final List<Expression> indices) {
        final Type exprType = lstArrayOrMap.resolveType(ctx.getResolver());
        if (exprType.getDescriptor().startsWith("[")) {
            visitArrayIndex(lstArrayOrMap, exprType, indices);
        } else {
            ctx.reportError("Expected array type but got: " + exprType.getClassName(), lstArrayOrMap);
        }
    }

    private void visitArrayIndex(final Expression array, final Type exprType, final List<Expression> indices) {
        array.accept(this);

        final TypeHandler arrayHandler = new AnyTypeHandler("LJava/lang/Object;");

        for (int i = 0; i + 1 < indices.size(); i++) {
            final Expression index = indices.get(i);
            final Type indexType = index.resolveType(ctx.getResolver());

            if (indexType == null) {
                ctx.reportError("Cannot resolve expression", index);
            } else if (!isAssignableFrom(indexType, Type.INT_TYPE)) {
                ctx.reportError("Expected type: int, but got: " + indexType.getClassName(), index);
            }

            index.accept(this);
            arrayHandler.arrayLoad(mv);
        }

        final Expression lastIndex = indices.get(indices.size() - 1);
        final Type lastIndexType = lastIndex.resolveType(ctx.getResolver());

        if (lastIndexType == null) {
            ctx.reportError("Cannot resolve expression", lastIndex);
        } else if (!isAssignableFrom(lastIndexType, Type.INT_TYPE)) {
            ctx.reportError("Expected type: int, but got: " + lastIndexType.getClassName(), lastIndex);
        } else {
            lastIndex.accept(this);

            if (getDim(exprType) > indices.size()) {
                arrayHandler.arrayLoad(mv);
            } else {
                getTypeHandler(exprType.getElementType()).arrayLoad(mv);
            }
        }
    }

    @Override
    public void visitTypeCast(final TypeCast cast) {
        final Type type = cast.resolveType(ctx.getResolver());
        final Type exprType = cast.getExpression().resolveType(ctx.getResolver());

        if (type == null) {
            ctx.reportError("Cannot resolve type: " + cast.getType(), cast.getType());
        } else if (exprType == null) {
            ctx.reportError("Cannot resolve type: " + cast.getType(), cast.getExpression());
        } else {
            cast.getExpression().accept(this);
            final TypeHandler to = getTypeHandler(type);
            final TypeHandler from = getTypeHandler(exprType);

            if (!to.cast(mv, from)) {
                if (isAssignableFrom(type, exprType)) {
                    mv.visitTypeInsn(CHECKCAST, to.getInternalName());
                } else {
                    ctx.reportError("Cast can never succeed from type: " + exprType.getClassName() + " to type: " +
                            type.getClassName(), cast);
                }
            }
        }
    }

    private void loadField(final FieldContext fieldCtx) {
        final Type type = fieldCtx.getTypeDescriptor();
        final TypeHandler handler = TypeUtilities.getTypeHandler(type);

        if (fieldCtx == ExpressionResolverImpl.ARRAY_LENGTH) {
            mv.visitInsn(ARRAYLENGTH);
        } else if (fieldCtx.isLocal()) {
            handler.load(mv, ctx.getScope().findVar(fieldCtx.getName()).getIndex());
        } else if (fieldCtx.isStatic()) {
            mv.visitFieldInsn(GETSTATIC, fieldCtx.getOwner(), fieldCtx.getName(), fieldCtx.getTypeDescriptor().getDescriptor());
        } else {
            mv.visitFieldInsn(GETFIELD, fieldCtx.getOwner(), fieldCtx.getName(), fieldCtx.getTypeDescriptor().getDescriptor());
        }
    }

    @Override
    public void visitExpressionFieldAccess(final ExpressionFieldAccess fa) {
        final Type precedingType = fa.getPrecedingExpr().resolveType(ctx.getResolver());
        final FieldContext fieldCtx = ctx.getResolver().resolveFieldCtx(fa.getPrecedingExpr(), fa.getFieldName());

        if (precedingType == null) {
            ctx.reportError("Cannot resolve expression", fa.getPrecedingExpr());
            return;
        }

        if (fieldCtx == null) {
            ctx.reportError("Cannot resolve field: " + fa.getFieldName(), fa);
            return;
        }

        fa.getPrecedingExpr().accept(this);
        loadField(fieldCtx);
    }

    @Override
    public void visitName(final QualifiedName name) {
        final FieldContext[] ctxList = ctx.getResolver().resolveFieldCtx(name);

        if (ctxList != null) {
            for (final FieldContext fieldCtx : ctxList) {
                loadField(fieldCtx);
            }
        } else {
            ctx.reportError("Cannot resolve field: " + name, name);
        }
    }

    @Override
    public void visitUnaryOp(final UnaryOp unaryOp) {
        final Expression expr = unaryOp.getExpression();
        final Type type = expr.resolveType(ctx.getResolver());

        if (type == null) {
            ctx.reportError("Cannot resolve expression", expr);
            return;
        }

        switch (unaryOp.getOperator()) {
            case "-":
                new BinaryOp(new Literal<>(0), "-", expr).accept(this);
                return;
            case "+":
                expr.accept(this);
                return;
            case "!":
                expr.accept(this);
                if (type.equals(Type.BOOLEAN_TYPE)) {
                    final Label after = new Label();
                    final Label falseLabel = new Label();

                    mv.visitJumpInsn(IFEQ, falseLabel);

                    mv.visitInsn(ICONST_0);
                    mv.visitJumpInsn(GOTO, after);

                    mv.visitLabel(falseLabel);

                    mv.visitInsn(ICONST_1);

                    mv.visitLabel(after);
                    return;
                }
        }

        ctx.reportError("No such operator: (" + unaryOp.getOperator() + type.getClassName() + ")", unaryOp);
    }

    public void pushFloat(final float value) {
        if (value == 0f) {
            mv.visitInsn(FCONST_0);
        } else if (value == 1f) {
            mv.visitInsn(FCONST_1);
        } else if (value == 2f) {
            mv.visitInsn(FCONST_2);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public void pushDouble(final double value) {
        if (value == 0D) {
            mv.visitInsn(DCONST_0);
        } else if (value == 1D) {
            mv.visitInsn(DCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public void pushInteger(final int value) {
        if (value >= -1 && value <= 5) {
            mv.visitInsn(ICONST_M1 + value + 1);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            mv.visitIntInsn(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
    }
}
