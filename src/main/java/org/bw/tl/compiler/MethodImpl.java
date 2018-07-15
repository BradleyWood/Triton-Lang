package org.bw.tl.compiler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.resolve.FieldContext;
import org.bw.tl.compiler.resolve.Operator;
import org.bw.tl.compiler.resolve.SymbolContext;
import org.bw.tl.compiler.types.TypeHandler;
import org.bw.tl.util.TypeUtilities;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;

import static org.bw.tl.util.TypeUtilities.*;

@EqualsAndHashCode(callSuper = false)
public @Data(staticConstructor = "of") class MethodImpl extends ASTVisitorBase implements Opcodes {

    private final @NotNull MethodVisitor mv;
    private final @NotNull MethodCtx ctx;

    @Override
    public void visitFunction(final Function function) {
        ctx.beginScope();

        final String[] parameterNames = function.getParameterNames();
        final QualifiedName[] parameterTypes = function.getParameterTypes();

        for (int i = 0; i < function.getParameterNames().length; i++) {
            ctx.getScope().putVar(parameterNames[i], ctx.resolveType(parameterTypes[i]), 0);
        }

        function.getBody().accept(this);

        mv.visitInsn(RETURN);
        ctx.endScope();
    }

    @Override
    public void visitField(final Field field) {
        final Type fieldType = ctx.resolveType(field.getType());

        if (fieldType == null) {
            ctx.reportError("Cannot resolve field type: " + field.getType(), field);
            return;
        }

        ctx.getScope().putVar(field.getName(), fieldType, field.getAccessModifiers());

        final Expression value = field.getInitialValue();
        final TypeHandler to = getTypeHandler(fieldType);

        if (value != null) {
            final Type valueType = value.resolveType(ctx.getResolver());

            if (valueType == null) {
                ctx.reportError("Cannot resolve expression", value);
                return;
            }

            final TypeHandler from = getTypeHandler(valueType);

            value.accept(this);

            if (!valueType.equals(fieldType) && !isAssignableFrom(valueType, fieldType)) {
                if (isAssignableWithImplicitCast(valueType, fieldType)) {
                    to.cast(mv, from);
                } else {
                    ctx.reportError("Expected type: " + fieldType.getClassName() + " but got: " + valueType.getClassName(), value);
                    return;
                }
            }
        } else {
            pushDefault(fieldType);
        }

        to.store(mv, ctx.getScope().findVar(field.getName()).getIndex());
    }

    @Override
    public void visitName(final QualifiedName name) {
        final FieldContext fieldCtx = ctx.getResolver().resolveFieldContext(name);

        if (fieldCtx != null) {
            final Type type = fieldCtx.getTypeDescriptor();
            final TypeHandler handler = TypeUtilities.getTypeHandler(type);

            if (fieldCtx.isLocal()) {
                handler.load(mv, ctx.getScope().findVar(name.toString()).getIndex());
            } else if (fieldCtx.isStatic()) {
                mv.visitFieldInsn(GETSTATIC, fieldCtx.getOwner(), fieldCtx.getName(), fieldCtx.getTypeDescriptor().getDescriptor());
            } else {
                mv.visitFieldInsn(GETFIELD, fieldCtx.getOwner(), fieldCtx.getName(), fieldCtx.getTypeDescriptor().getDescriptor());
            }
        } else {
            ctx.reportError("Cannot resolve field: " + name, name);
        }
    }

    @Override
    public void visitWhile(final WhileLoop whileLoop) {
        final Expression condition = whileLoop.getCondition();
        final Type type = condition.resolveType(ctx.getResolver());

        if (type == null) {
            ctx.reportError("Cannot resolve expression", condition);
            return;
        }

        if (type.equals(Type.BOOLEAN_TYPE)) {
            final Label conditionalLabel = new Label();
            final Label endLabel = new Label();

            mv.visitLabel(conditionalLabel);
            condition.accept(this);
            mv.visitJumpInsn(IFEQ, endLabel);

            ctx.beginScope();
            whileLoop.getBody().accept(this);
            ctx.endScope();

            mv.visitJumpInsn(GOTO, conditionalLabel);
            mv.visitLabel(endLabel);
        } else {
            ctx.reportError("Expected boolean, found: " + type.getClassName(), condition);
        }
    }

    @Override
    public void visitIf(final IfStatement ifStatement) {
        final Expression condition = ifStatement.getCondition();
        final Type type = condition.resolveType(ctx.getResolver());
        final Node elseBlock = ifStatement.getElseBody();

        if (type == null) {
            ctx.reportError("Cannot resolve expression", condition);
            return;
        }

        if (type.equals(Type.BOOLEAN_TYPE)) {
            final Label after = new Label();
            final Label elseLabel = new Label();

            condition.accept(this);

            if (elseBlock != null) {
                mv.visitJumpInsn(IFEQ, elseLabel);

                ctx.beginScope();
                ifStatement.getBody().accept(this);
                ctx.endScope();

                mv.visitJumpInsn(GOTO, after);

                mv.visitLabel(elseLabel);

                ctx.beginScope();
                elseBlock.accept(this);
                ctx.endScope();
            } else {
                mv.visitJumpInsn(IFEQ, after);

                ctx.beginScope();
                ifStatement.getBody().accept(this);
                ctx.endScope();
            }

            mv.visitLabel(after);
        } else {
            ctx.reportError("Expected boolean, found: " + type.getClassName(), condition);
        }
    }

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

            int opcode = funCtx.isStatic() ? INVOKESTATIC : INVOKEVIRTUAL;

            mv.visitMethodInsn(opcode, funCtx.getOwner(), funCtx.getName(), funCtx.getTypeDescriptor().getDescriptor(), false);

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
    public void visitAssignment(final Assignment assignment) {
        final FieldContext fieldCtx = ctx.getResolver().resolveFieldContext(assignment.getPrecedingExpr(), assignment.getName());
        final Type valueType = assignment.resolveType(ctx.getResolver());

        if (fieldCtx == null) {
            ctx.reportError("Cannot resolve field", assignment);
            return;
        }

        if (valueType == null) {
            ctx.reportError("Cannot resolve expression", assignment.getValue());
            return;
        }

        final TypeHandler to = getTypeHandler(fieldCtx.getTypeDescriptor());
        final TypeHandler from = getTypeHandler(valueType);

        assignment.getValue().accept(this);

        if (!fieldCtx.getTypeDescriptor().equals(valueType) && !isAssignableFrom(valueType, fieldCtx.getTypeDescriptor())) {
            if (isAssignableWithImplicitCast(valueType, fieldCtx.getTypeDescriptor())) {
                to.cast(mv, from);
            } else {
                ctx.reportError("Expected type: " + fieldCtx.getTypeDescriptor().getClassName() + " but got: " +
                        valueType.getClassName(), assignment.getValue());
            }
        }

        if (fieldCtx.isLocal()) {
            to.store(mv, ctx.getScope().findVar(fieldCtx.getName()).getIndex());
        } else if (fieldCtx.isStatic()) {
            mv.visitFieldInsn(PUTSTATIC, fieldCtx.getOwner(), fieldCtx.getName(), fieldCtx.getTypeDescriptor().getDescriptor());
        } else {
            if (!ctx.isStatic() && assignment.getPrecedingExpr() == null) {
                mv.visitVarInsn(ALOAD, 0); // put this on stack
            } else if (assignment.getPrecedingExpr() != null) {
                assignment.getPrecedingExpr().accept(this);
            } else if (ctx.isStatic()){
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

        if (binaryOp.getOperator().equals("&&")) {
            andOperator(lhs, rhs, leftType, rightType);
            return;
        } else if (binaryOp.getOperator().equals("||")) {
            orOperator(lhs, rhs, leftType, rightType);
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
            } else {
                mv.visitLdcInsn(value);
            }
        } else {
            System.err.println("Invalid literal type");
        }
    }

    @Override
    public void visitReturn(final Return returnStmt) {
        final Expression expr = returnStmt.getExpression();
        final Type exprType = expr.resolveType(ctx.getResolver());

        if (exprType == null) {
            ctx.reportError("Cannot resolve expression", expr);
            return;
        }

        final Type returnType = ctx.getReturnType();
        final TypeHandler retTypeHandler = getTypeHandler(returnType);

        expr.accept(this);

        if (isAssignableWithImplicitCast(exprType, returnType)) {
            final TypeHandler from = getTypeHandler(exprType);
            retTypeHandler.cast(mv, from);
        } else if (!isAssignableFrom(exprType, returnType) && !exprType.equals(returnType)) {
            ctx.reportError("Expected type: " + returnType.getClassName() + " but" +
                    " got " + exprType.getClassName(), expr);
            return;
        }

        retTypeHandler.ret(mv);
    }

    private void pushDefault(final Type type) {
        if (type.equals(Type.BOOLEAN_TYPE) || type.equals(Type.BYTE_TYPE) || type.equals(Type.CHAR_TYPE)
                || type.equals(Type.SHORT_TYPE) || type.equals(Type.INT_TYPE)) {
            mv.visitInsn(ICONST_0);
        } else if (type.equals(Type.FLOAT_TYPE)) {
            mv.visitInsn(FCONST_0);
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            mv.visitInsn(DCONST_0);
        } else if (type.equals(Type.LONG_TYPE)) {
            mv.visitInsn(LCONST_0);
        } else {
            mv.visitInsn(ACONST_NULL);
        }
    }

    private void pushFloat(final float value) {
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

    private void pushDouble(final double value) {
        if (value == 0D) {
            mv.visitInsn(DCONST_0);
        } else if (value == 1D) {
            mv.visitInsn(DCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    private void pushInteger(final int value) {
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
