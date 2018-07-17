package org.bw.tl.compiler.resolve;

import lombok.Data;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.List;

import static org.bw.tl.util.TypeUtilities.isMethodType;

public @Data class ExpressionResolverImpl implements ExpressionResolver {

    private final SymbolResolver symbolResolver;
    private final Module module;
    private final File file;
    private final Scope scope;

    @Nullable
    @Override
    public Type resolveBinaryOp(@NotNull final BinaryOp bop) {
        Type lhs = bop.getLeftSide().resolveType(this);
        Type rhs = bop.getRightSide().resolveType(this);

        if (lhs != null) {
            if (isMethodType(lhs)) {
                lhs = lhs.getReturnType();
            }
        } else {
            return null;
        }

        if (rhs != null) {
            if (isMethodType(rhs))
                rhs = rhs.getReturnType();
        } else {
            return null;
        }

        if (bop.getOperator().equals("&&") || bop.getOperator().equals("||"))
            return Type.BOOLEAN_TYPE;

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

        return ctx.getTypeDescriptor().getReturnType();
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

    @Nullable
    @Override
    public SymbolContext resolveCallCtx(@NotNull final Call call) {
        final Type[] parameterTypes = new Type[call.getParameters().size()];
        final Expression preceding = call.getPrecedingExpr();

        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = call.getParameters().get(i).resolveType(this);
        }

        if (preceding != null) {
            if (preceding instanceof QualifiedName) {
                Type owner = resolveName((QualifiedName) preceding);
                if (owner == null)
                    owner = symbolResolver.resolveType((QualifiedName) preceding);
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

        return symbolResolver.resolveFunctionContext(module, call.getName(), parameterTypes);
    }

    @Nullable
    @Override
    public SymbolContext resolveConstructorContext(@NotNull final New newStmt) {
        final List<Expression> expressionList = newStmt.getParameters();
        final Type[] types = new Type[expressionList.size()];

        for (int i = 0; i < types.length; i++) {
            types[i] = expressionList.get(i).resolveType(this);
            if (types[i] == null)
                return null;
        }

        return symbolResolver.resolveConstructor(newStmt.getType(), types);
    }

    @Nullable
    @Override
    public Type resolveConstructor(@NotNull final New newStmt) {
        if (newStmt.isArray()) {
            final Type componentType = symbolResolver.resolveType(newStmt.getType());

            if (componentType == null)
                return null;

            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < newStmt.getParameters().size(); i++) {
                sb.append('[');
            }

            sb.append(componentType.getDescriptor());
            return Type.getType(sb.toString());
        }

        final SymbolContext ctx = resolveConstructorContext(newStmt);
        final Type type = symbolResolver.resolveType(newStmt.getType());

        if (ctx != null && type != null)
            return type;

        return null;
    }

    @Nullable
    @Override
    public Type resolveTypeCast(@NotNull final TypeCast typeCast) {
        return symbolResolver.resolveType(typeCast.getType());
    }

    @Override
    public Type resolveTypeName(final TypeName typeName) {
        final Type componentType = symbolResolver.resolveType(typeName);

        if (typeName.getDim() == 0 || componentType == null)
            return componentType;

        final StringBuilder descBuilder = new StringBuilder();

        for (int i = 0; i < typeName.getDim(); i++) {
            descBuilder.append('[');
        }

        descBuilder.append(componentType.getDescriptor());

        return Type.getType(descBuilder.toString());
    }

    @Nullable
    @Override
    public FieldContext resolveFieldContext(@NotNull final QualifiedName name) {
        if (scope != null) {
            Scope.Var var = scope.findVar(name.getNames()[0]);
            if (var != null) {
                return new FieldContext(var.getName(), module.getInternalName(), var.getType(), var.getModifiers(), true);
            }
        }

        return symbolResolver.resolveField(name);
    }

    @Nullable
    @Override
    public FieldContext resolveFieldContext(@Nullable final Expression preceding, @NotNull final String name) {
        if (preceding == null)
            return resolveFieldContext(QualifiedName.of(name));

        final Type type = preceding.resolveType(this);

        if (type != null) {
            return symbolResolver.resolveField(type, name);
        }

        return null;
    }
}
