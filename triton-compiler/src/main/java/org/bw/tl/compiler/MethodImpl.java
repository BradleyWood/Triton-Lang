package org.bw.tl.compiler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.resolve.*;
import org.bw.tl.compiler.types.TypeHandler;
import org.bw.tl.util.TypeUtilities;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

import static org.bw.tl.util.TypeUtilities.*;

@EqualsAndHashCode(callSuper = false)
public @Data(staticConstructor = "of") class MethodImpl extends ASTVisitorBase implements Opcodes {

    private final @NotNull MethodVisitor mv;
    private final @NotNull MethodCtx ctx;
    private ExpressionImpl expressionImpl;

    private final Label endOfFunctionLabel = new Label();

    @Override
    public void visitFunction(final Function function) {
        ctx.beginScope();

        if (!ctx.isStatic()) {
            ctx.getScope().putVar(" __this__ ", Type.getType(Object.class), 0);
        }

        final String[] parameterNames = function.getParameterNames();
        final TypeName[] parameterTypes = function.getParameterTypes();
        final List<Modifier>[] parameterModifiers = function.getParameterModifiers();

        for (int i = 0; i < function.getParameterNames().length; i++) {
            final int modifiers = parameterModifiers[i].stream().mapToInt(Modifier::getValue).sum();
            final Label startLabel = new Label();

            mv.visitLabel(startLabel);

            if (!ctx.getScope().putVar(parameterNames[i], parameterTypes[i].resolveType(ctx.getResolver()), modifiers)) {
                ctx.reportError("Duplicate function parameter names", function);
            } else {
                final int idx = ctx.getScope().findVar(parameterNames[i]).getIndex();
                final Type type = parameterTypes[i].resolveType(ctx.getResolver());

                if (type != null) {
                    mv.visitLocalVariable(parameterNames[i], type.getDescriptor(), null, startLabel, endOfFunctionLabel, idx);
                } else {
                    ctx.reportError("Cannot resolve symbol", parameterTypes[i]);
                }
            }
        }

        if (function.isShortForm()) {
            if (function.getBody() instanceof Expression) {
                final Expression retVal = (Expression) function.getBody();
                final Type retType = retVal.resolveType(ctx.getResolver());
                if (ctx.getReturnType().equals(Type.VOID_TYPE)) {
                    // short-form function definition with where void return type is explicitly requested
                    retVal.accept(this);

                    if (!retType.equals(Type.VOID_TYPE)) {
                        if (retType.equals(Type.LONG_TYPE) || retType.equals(Type.DOUBLE_TYPE)) {
                            mv.visitInsn(POP2);
                        } else {
                            mv.visitInsn(POP);
                        }
                    }
                } else {
                    visitReturn(new Return(retVal));
                }
            } else {
                ctx.reportError("Illegal return statement", function);
            }
        } else {
            function.getBody().accept(this);
        }

        mv.visitInsn(RETURN);

        mv.visitLabel(endOfFunctionLabel);


        ctx.endScope();
    }

    private Type getImplicitType(final Expression expr) {
        final Type type = expr.resolveType(ctx.getResolver());

        if (type == null)
            return null;

        if (isAssignableWithImplicitCast(type, Type.INT_TYPE))
            return Type.INT_TYPE;

        return type;
    }

    @Override
    public void visitField(final Field field) {
        Expression value = field.getInitialValue();
        final Type fieldType;

        if (field.getType() != null) {
            fieldType = field.getType().resolveType(ctx.getResolver());
        } else {
            fieldType = getImplicitType(value);

            if (fieldType == null) {
                ctx.reportError("Cannot infer type", field);
                return;
            }
        }

        if (fieldType == null) {
            ctx.reportError("Cannot resolve field type: " + field.getType(), field);
            return;
        }

        if (!ctx.getScope().putVar(field.getName(), fieldType, field.getAccessModifiers()))
            ctx.reportError("Field: " + field.getName() + " has already been defined", field);

        final TypeHandler to = getTypeHandler(fieldType);

        if (value == null) {
            value = getDefault(fieldType);
        }

        visitAssignment(new Assignment(null, field.getName(), value));
//        to.store(mv, ctx.getScope().findVar(field.getName()).getIndex());
    }

    public Literal getDefault(final Type type) {
        if (type.equals(Type.BYTE_TYPE) || type.equals(Type.CHAR_TYPE)
                || type.equals(Type.SHORT_TYPE) || type.equals(Type.INT_TYPE)) {
            return new Literal(0);
        } else if (type.equals(Type.FLOAT_TYPE)) {
            return new Literal(0f);
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            return new Literal(0.0);
        } else if (type.equals(Type.LONG_TYPE)) {
            return new Literal(0l);
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            return new Literal(false);
        } else {
            return new Literal(null);
        }
    }

    public void pushDefault(final Type type) {
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

    @Override
    public void visitExpressionIndices(final ExpressionIndex expressionIndex) {
        expressionImpl.visitExpressionIndices(expressionIndex);
    }

    @Override
    public void visitTypeCast(final TypeCast cast) {
        expressionImpl.visitTypeCast(cast);
    }

    @Override
    public void visitExpressionFieldAccess(final ExpressionFieldAccess fa) {
        expressionImpl.visitExpressionFieldAccess(fa);
    }

    @Override
    public void visitName(final QualifiedName name) {
        expressionImpl.visitName(name);
    }

    @Override
    public void visitUnaryOp(final UnaryOp unaryOp) {
        expressionImpl.visitUnaryOp(unaryOp);
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
    public void visitFor(final ForLoop forLoop) {
        ctx.beginScope();

        if (forLoop.getInit() != null)
            forLoop.getInit().accept(this);

        final Expression condition = forLoop.getCondition();

        final Label conditionalLabel = new Label();
        final Label endLabel = new Label();

        mv.visitLabel(conditionalLabel);

        if (condition != null) {
            final Type type = condition.resolveType(ctx.getResolver());

            if (type != null && type.equals(Type.BOOLEAN_TYPE)) {
                condition.accept(this);
                mv.visitJumpInsn(IFEQ, endLabel);
            } else if (type != null) {
                ctx.reportError("Expected boolean, found: " + type.getClassName(), condition);
            } else {
                ctx.reportError("Cannot resolve expression", condition);
            }
        }

        if (forLoop.getBody() != null)
            forLoop.getBody().accept(this);

        forLoop.getUpdate().forEach(e -> e.accept(this));

        mv.visitJumpInsn(GOTO, conditionalLabel);
        mv.visitLabel(endLabel);

        ctx.endScope();
    }

    @Override
    public void visitForEach(final ForEachLoop forEachLoop) {
        ctx.beginScope();

        final Expression iterable = forEachLoop.getIterableExpression();
        final Type iterableType = iterable.resolveType(ctx.getResolver());

        if (iterableType == null) {
            ctx.reportError("Cannot resolve expression", iterable);
            return;
        }

        int dim = TypeUtilities.getDim(iterableType);

        if (dim == 0) {
            ctx.reportError("Expected array type but got " + iterableType.getClassName(), iterable);
            return;
        }

        final Field field = forEachLoop.getField();
        final Type expectedType = TypeUtilities.setDim(iterableType, dim - 1);
        Type fieldType = expectedType;

        if (field.getType() != null) {
            fieldType = field.getType().resolveType(ctx.getResolver());

            if (fieldType == null) {
                ctx.reportError("Cannot resolve type: " + field.getType(), field);
                return;
            }

            if (!expectedType.equals(fieldType)) {
                ctx.reportError("Expected type " + expectedType.getClassName() + " but got " + fieldType.getClassName(), field);
                return;
            }
        }

        ctx.getScope().putVar(" __ITERABLE__ ", iterableType, 0);
        iterable.accept(this);
        mv.visitVarInsn(ASTORE, ctx.getScope().findVar(" __ITERABLE__ ").getIndex());

        ctx.getScope().putVar(" __COUNTER__ ", Type.INT_TYPE, 0);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, ctx.getScope().findVar(" __COUNTER__ ").getIndex());

        final Label conditionalLabel = new Label();
        final Label endLabel = new Label();

        mv.visitLabel(conditionalLabel);

        mv.visitVarInsn(ILOAD, ctx.getScope().findVar(" __COUNTER__ ").getIndex());
        mv.visitVarInsn(ALOAD, ctx.getScope().findVar(" __ITERABLE__ ").getIndex());
        mv.visitInsn(ARRAYLENGTH);

        final Operator operator = Operator.getOperator("<", Type.INT_TYPE, Type.INT_TYPE);
        operator.applyCmp(mv, endLabel);

        ctx.beginScope();

        ctx.getScope().putVar(field.getName(), fieldType, 0);

        final TypeHandler fieldTypeHandler = TypeUtilities.getTypeHandler(fieldType);
        mv.visitVarInsn(ALOAD, ctx.getScope().findVar(" __ITERABLE__ ").getIndex());
        mv.visitVarInsn(ILOAD, ctx.getScope().findVar(" __COUNTER__ ").getIndex());
        fieldTypeHandler.arrayLoad(mv);

        fieldTypeHandler.store(mv, ctx.getScope().findVar(field.getName()).getIndex());

        forEachLoop.getBody().accept(this);
        mv.visitIincInsn(ctx.getScope().findVar(" __COUNTER__ ").getIndex(), 1);

        ctx.endScope();

        mv.visitJumpInsn(GOTO, conditionalLabel);
        mv.visitLabel(endLabel);

        ctx.endScope();
    }

    @Override
    public void visitNew(final New newExpr) {
        expressionImpl.visitNew(newExpr);
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
        expressionImpl.visitCall(call);
    }

    @Override
    public void visitAssignment(final Assignment assignment) {
        expressionImpl.visitAssignment(assignment);
    }

    @Override
    public void visitBinaryOp(final BinaryOp binaryOp) {
        expressionImpl.visitBinaryOp(binaryOp);
    }

    @Override
    public void visitLiteral(final Literal literal) {
        expressionImpl.visitLiteral(literal);
    }

    @Override
    public void visitReturn(final Return returnStmt) {
        final Expression expr = returnStmt.getExpression();

        if (ctx.getReturnType() == Type.VOID_TYPE || expr == null) {
            if (ctx.getReturnType() == Type.VOID_TYPE && expr == null) {
                mv.visitInsn(RETURN);
            } else {
                ctx.reportError("Illegal return value", returnStmt.getExpression());
            }
        } else {
            final Type exprType = expr.resolveType(ctx.getResolver());

            if (expr instanceof Literal) {
                final Object val = ((Literal) expr).getValue();

                if (val == null) {
                    mv.visitInsn(ACONST_NULL);
                    mv.visitInsn(ARETURN);
                    return;
                }
            }

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
    }
}
