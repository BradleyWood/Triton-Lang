package org.bw.tl.compiler.resolve;

import lombok.Data;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.LinkedList;
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
        final FieldContext[] ctx = resolveFieldContext(name);

        if (ctx == null)
            return null;

        return ctx[ctx.length - 1].getTypeDescriptor();
    }

    @Nullable
    @Override
    public SymbolContext resolveCallCtx(@NotNull final Call call) {
        final Type[] parameterTypes = new Type[call.getParameters().size()];
        final Expression preceding = call.getPrecedingExpr();

        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = call.getParameters().get(i).resolveType(this);
            if (parameterTypes[i] == null) {
                return null;
            }
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
    public FieldContext[] resolveFieldContext(@NotNull final QualifiedName name) {
        final FieldContext[] localCtx = resolveFieldFromLocalVar(name);

        if (localCtx != null) {
            return localCtx;
        }

        final FieldContext[] localFieldCtx = resolveFieldFromLocalField(name);

        if (localFieldCtx != null)
            return localFieldCtx;

        return resolveExternalField(name);
    }

    private FieldContext[] resolveExternalField(@NotNull final QualifiedName name) {
        if (name.length() <= 1)
            return null;

        final List<FieldContext> ctxList = new LinkedList<>();
        final String[] names = name.getNames();

        outer:
        for (final QualifiedName imp : file.getImports()) {
            int idx = imp.length();

            if (name.equals(imp)) // name is fqn not a field
                return null;

            if (name.length() <= imp.length() || !imp.equals(name.subname(0, imp.length()))) {
                if (imp.endsWith(names[0])) {
                    idx = 1;
                } else {
                    continue;
                }
            }

            Type type = symbolResolver.resolveType(imp);

            if (type == null)
                return null;

            for (int i = idx; i < names.length; i++) {
                final FieldContext ctx = symbolResolver.resolveField(type, names[i]);

                if (ctx == null)
                    return null;

                ctxList.add(ctx);
                type = ctx.getTypeDescriptor();
            }

            return ctxList.toArray(new FieldContext[0]);
        }

        return null;
    }

    private FieldContext[] resolveFieldFromLocalVar(@NotNull final QualifiedName fqn) {
        final List<FieldContext> ctxList = new LinkedList<>();

        if (scope != null) {
            Scope.Var var = scope.findVar(fqn.getNames()[0]);
            if (var != null) {
                ctxList.add(new FieldContext(var.getName(), module.getInternalName(), var.getType(), var.getModifiers(), true));

                if (fqn.length() > 1) {
                    final String[] names = fqn.subname(1, fqn.length()).getNames();
                    Type type = var.getType();
                    for (final String n : names) {
                        final FieldContext ctxN = symbolResolver.resolveField(type, n);
                        if (ctxN == null)
                            return null;
                        ctxList.add(ctxN);
                        type = ctxN.getTypeDescriptor();
                    }
                }

                return ctxList.toArray(new FieldContext[0]);
            }
        }

        return null;
    }

    private FieldContext[] resolveFieldFromLocalField(@NotNull final QualifiedName fqn) {
        final List<FieldContext> ctxList = new LinkedList<>();
        final String[] names = fqn.getNames();

        FieldContext ctx = symbolResolver.resolveField(Type.getType(module.getDescriptor()), names[0]);

        if (ctx == null)
            return null;

        ctxList.add(ctx);

        for (int i = 1; i < names.length; i++) {
            ctx = symbolResolver.resolveField(ctx.getTypeDescriptor(), names[i]);

            if (ctx == null)
                return null;

            ctxList.add(ctx);
        }

        return ctxList.toArray(new FieldContext[0]);
    }

    @Nullable
    @Override
    public FieldContext resolveFieldContext(@Nullable final Expression preceding, @NotNull final String name) {
        if (preceding != null) {
            final Type type = preceding.resolveType(this);

            if (type != null) {
                return symbolResolver.resolveField(type, name);
            }

            return null;
        }

        if (scope != null) {
            final Scope.Var var = scope.findVar(name);

            if (var != null) {
                return new FieldContext(var.getName(), module.getInternalName(), var.getType(), var.getModifiers(), true);
            }
        }

        return symbolResolver.resolveField(Type.getType(module.getDescriptor()), name);
    }
}