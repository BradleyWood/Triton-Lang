package org.bw.tl.compiler.resolve;

import lombok.Data;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

public @Data class ExpressionResolverImpl implements ExpressionResolver {

    private final SymbolResolver symbolResolver;
    private final Module module;
    private final File file;
    private final Scope scope;

    @Nullable
    @Override
    public Type resolveBinaryOp(@NotNull final BinaryOp bop) {
        final Type lhs = bop.getLeftSide().resolveType(this);
        final Type rhs = bop.getRightSide().resolveType(this);

        final Operator operator = Operator.getOperator(bop.getOperator(), lhs, rhs);

        if (operator != null) {
            return operator.getResultType();
        }

        return null;
    }

    @Nullable
    @Override
    public Type resolveUnaryOp(@NotNull final UnaryOp op) {
        return op.getExpression().resolveType(this);
    }

    @Nullable
    @Override
    public Type resolveCall(@NotNull final Call call) {
        final SymbolContext ctx = resolveCallCtx(call);

        if (ctx == null)
            return null;

        return ctx.getTypeDescriptor();
    }

    @Nullable
    @Override
    public Type resolveLiteral(@NotNull final Literal literal) {
        if (literal.getValue() instanceof String) {
            return Type.getType(String.class);
        } else if (literal.getValue() instanceof Boolean) {
            return Type.getType(Boolean.TYPE);
        } else if (literal.getValue() instanceof Character) {
            return Type.getType(Character.TYPE);
        } else if (literal.getValue() instanceof Float) {
            return Type.getType(Float.TYPE);
        } else if (literal.getValue() instanceof Double) {
            return Type.getType(Double.TYPE);
        } else if (literal.getValue() instanceof Number) {
            long val = ((Number) literal.getValue()).longValue();
            if (val <= Byte.MAX_VALUE && val >= Byte.MIN_VALUE) {
                return Type.getType(Byte.TYPE);
            } else if (val <= Short.MAX_VALUE && val >= Short.MIN_VALUE) {
                return Type.getType(Short.TYPE);
            } else if (val <= Integer.MAX_VALUE && val >= Integer.MIN_VALUE) {
                return Type.getType(Integer.TYPE);
            } else {
                return Type.getType(Long.TYPE);
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Type resolveName(@NotNull final QualifiedName name) {
        final FieldContext ctx = resolveFieldContext(name);

        if (ctx == null)
            return null;

        return ctx.getTypeDescriptor();
    }

    @Override
    public SymbolContext resolveCallCtx(final Call call) {
        final Type[] parameterTypes = new Type[call.getParameters().size()];
        final Expression preceding = call.getPrecedingExpr();

        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = call.getParameters().get(i).resolveType(this);
        }

        if (preceding != null) {
            if (preceding instanceof QualifiedName) {
                Type owner = resolveName((QualifiedName) preceding);
                if (owner == null)
                    owner = Type.getType(((QualifiedName) preceding).getDesc());
                if (owner == null)
                    return null;

                return symbolResolver.resolveFunction(owner, call.getName(), parameterTypes);
            } else {
                final Type objType = preceding.resolveType(this);

                if (objType == null)
                    return null;

                return symbolResolver.resolveFunction(objType, call.getName(), parameterTypes);
            }
        }
        final Function function = module.resolveFunction(call.getName(), parameterTypes);
        final Type type = module.resolveFunctionType(call.getName(), parameterTypes);

        if (function == null)
            return null;

        return new SymbolContext(function.getName(), type, function.getAccessModifiers());
    }

    @Override
    public FieldContext resolveFieldContext(final QualifiedName name) {
        if (scope != null) {
            Scope.Var var = scope.findVar(name.getNames()[0]);
            if (var != null) {
                return new FieldContext(var.getName(), var.getType(), 0, true);
            }
        }

        return symbolResolver.resolveField(name);
    }
}
