package org.bw.tl.compiler.resolve;

import lombok.Data;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.Scope;
import org.bw.tl.compiler.types.MethodComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;

import static org.bw.tl.util.TypeUtilities.*;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public @Data class ExpressionResolverImpl implements ExpressionResolver {

    public static FieldContext ARRAY_LENGTH = new FieldContext("length", "java/lang/Object",
            Type.INT_TYPE, ACC_PUBLIC, false);

    private final Clazz clazz;

    @NotNull
    private final List<Clazz> classpath;
    private final ClassLoader loader;
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

        if ("&&".equals(bop.getOperator()) || "||".equals(bop.getOperator()))
            return Type.BOOLEAN_TYPE;

        if ((isAssignableFrom(lhs, Type.getType(String.class)) || isAssignableFrom(rhs, Type.getType(String.class))) &&
                "+".equals(bop.getOperator())) {
            return Type.getType(String.class);
        }

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

    @Override
    public Type resolveWhen(@NotNull final When when) {
        return Type.getType(Object.class);
    }

    private Class<?> resolveClass(final String name) throws ClassNotFoundException {
        return loader.loadClass(name);
    }

    private Type resolveNode(final Node node) {
        if (node instanceof Block) {
            final Block block = (Block) node;
            final List<Node> stmts = block.getStatements();

            if (!stmts.isEmpty())
                return resolveNode(stmts.get(stmts.size() - 1));
        } else if (node instanceof Expression) {
            final Expression expr = (Expression) node;
            return expr.resolveType(this);
        }

        return Type.VOID_TYPE;
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
        final FieldContext[] ctx = resolveFieldCtx(name);

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
                    owner = resolveType((QualifiedName) preceding);
                if (owner == null)
                    return null;

                return resolveFunctionCtx(owner, call.getName(), parameterTypes);
            } else {
                final Type objType = preceding.resolveType(this);

                if (objType == null)
                    return null;

                return resolveFunctionCtx(objType, call.getName(), parameterTypes);
            }
        }

        final SymbolContext ctx = resolveFunctionContext(call.getName(), parameterTypes);

        if (ctx != null)
            return ctx;

        return resolveCallFromStaticImports(call.getName(), parameterTypes);
    }

    @Nullable
    @Override
    public SymbolContext resolveConstructorCtx(@NotNull final New newStmt) {
        final List<Expression> expressionList = newStmt.getParameters();
        final Type[] types = new Type[expressionList.size()];

        for (int i = 0; i < types.length; i++) {
            types[i] = expressionList.get(i).resolveType(this);
            if (types[i] == null)
                return null;
        }

        return resolveConstructorCtx(newStmt.getType(), types);
    }

    @Nullable
    @Override
    public Type resolveConstructor(@NotNull final New newStmt) {
        if (newStmt.isArray()) {
            final Type componentType = resolveType(newStmt.getType());

            if (componentType == null)
                return null;

            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < newStmt.getParameters().size(); i++) {
                sb.append('[');
            }

            sb.append(componentType.getDescriptor());
            return Type.getType(sb.toString());
        }

        final SymbolContext ctx = resolveConstructorCtx(newStmt);
        final Type type = resolveType(newStmt.getType());

        if (ctx != null && type != null)
            return type;

        return null;
    }

    @Nullable
    @Override
    public Type resolveTypeCast(@NotNull final TypeCast typeCast) {
        return resolveType(typeCast.getType());
    }

    @Override
    public Type resolveTypeName(final TypeName typeName) {
        final Type componentType = resolveType(typeName);

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
    public FieldContext[] resolveFieldCtx(@NotNull final QualifiedName name) {
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

        for (final QualifiedName imp : clazz.getImports()) {
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

            Type type = resolveType(imp);

            if (type == null)
                return null;

            for (int i = idx; i < names.length; i++) {
                final FieldContext ctx = resolveFieldCtx(type, names[i]);

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
                ctxList.add(new FieldContext(var.getName(), clazz.getInternalName(), var.getType(), var.getModifiers(), true));

                if (fqn.length() > 1) {
                    final String[] names = fqn.subname(1, fqn.length()).getNames();
                    Type type = var.getType();
                    for (final String n : names) {
                        final FieldContext ctxN = resolveFieldCtx(type, n);
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

        FieldContext ctx = resolveFieldCtx(Type.getType(clazz.getDescriptor()), names[0]);

        if (ctx == null)
            ctx = resolveFieldFromStaticImports(names[0]);

        if (ctx == null)
            return null;

        ctxList.add(ctx);

        for (int i = 1; i < names.length; i++) {
            ctx = resolveFieldCtx(ctx.getTypeDescriptor(), names[i]);

            if (ctx == null)
                return null;

            ctxList.add(ctx);
        }

        return ctxList.toArray(new FieldContext[0]);
    }

    @Nullable
    @Override
    public FieldContext resolveFieldCtx(@Nullable final Expression preceding, @NotNull final String name) {
        if (preceding != null) {
            Type type = preceding.resolveType(this);

            if (type == null && preceding instanceof QualifiedName) {
                type = resolveType((QualifiedName) preceding);
            }

            if (type != null) {
                return resolveFieldCtx(type, name);
            }

            return null;
        }

        if (scope != null) {
            final Scope.Var var = scope.findVar(name);

            if (var != null) {
                return new FieldContext(var.getName(), clazz.getInternalName(), var.getType(), var.getModifiers(), true);
            }
        }

        return resolveFieldCtx(Type.getType(clazz.getDescriptor()), name);
    }

    @Nullable
    private SymbolContext resolveFunctionCtx(@NotNull final Type owner, @NotNull final String name, @NotNull final Type... parameterTypes) {
        for (final Clazz module : classpath) {
            if (module.getDescriptor().equals(owner.getDescriptor())) {
                final Function fun = resolveFunctionCtx(module, name, parameterTypes);
                if (fun != null) {
                    final Type retType = getTypeFromName(fun.getType());

                    if (retType == null)
                        return null;

                    final Type methodType = resolveFunctionCtx(clazz, fun);

                    if (methodType == null)
                        return null;

                    return new SymbolContext(fun.getName(), module.getInternalName(), methodType, fun.getAccessModifiers());
                }
            }
        }

        try {
            return resolveFunctionCtx(resolveClass(owner.getClassName()), name, parameterTypes);
        } catch (ClassNotFoundException ignored) {
        }

        return null;
    }

    @Nullable
    private SymbolContext resolveFunctionCtx(final @NotNull Class<?> clazz, @NotNull final String name, @NotNull final Type... parameterTypes) {
        final List<Executable> candidates = new LinkedList<>();

        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                final Class<?>[] types = method.getParameterTypes();

                if (types.length != parameterTypes.length)
                    continue;

                candidates.add(method);
            }
        }

        int best = selectExecutable(candidates, parameterTypes);
        if (best != -1) {
            final Method method = (Method) candidates.get(best);
            return new SymbolContext(name, clazz.getName().replace(".", "/"), Type.getType(method),
                    method.getModifiers());
        }

        return null;
    }

    @Nullable
    public Type resolveType(@NotNull final QualifiedName name) {
        if (name.length() == 0)
            return null;

        QualifiedName typeName = name;

        for (final QualifiedName imp : clazz.getImports()) {
            if (imp.endsWith(name.getNames()[0])) {
                typeName = imp;
            }
        }

        for (final Clazz module : classpath) {
            if (module.getModuleClassName().equals(typeName.getName())) {
                return Type.getType(module.getDescriptor());
            }
        }

        try {
            return Type.getType(resolveClass(typeName.getName()));
        } catch (ClassNotFoundException ignored) {
        }

        return getTypeFromName(name);
    }

    @Nullable
    private SymbolContext resolveConstructorCtx(@NotNull final QualifiedName owner, @NotNull final Type... parameterTypes) {
        final Type type = resolveType(owner);

        if (type != null)
            return resolveConstructorCtx(type, parameterTypes);

        return null;
    }

    @Nullable
    private SymbolContext resolveConstructorCtx(@NotNull final Type owner, @NotNull final Type... parameterTypes) {
        try {
            final Class<?> clazz = resolveClass(owner.getClassName());
            final List<Executable> constructorList = new LinkedList<>();

            for (final Constructor<?> constructor : clazz.getConstructors()) {
                final Class<?>[] types = constructor.getParameterTypes();

                if (types.length != parameterTypes.length)
                    continue;

                constructorList.add(constructor);
            }

            int best = selectExecutable(constructorList, parameterTypes);

            if (best == -1)
                return null;

            final Constructor constructor = (Constructor) constructorList.get(best);

            return new SymbolContext("<init>", owner.getInternalName(), Type.getType(constructor),
                    constructor.getModifiers());
        } catch (final ClassNotFoundException ignored) {
        }

        return null;
    }

    @Nullable
    private FieldContext resolveFieldCtx(@NotNull final Type owner, @NotNull final String name) {
        if (owner.getDescriptor().startsWith("[") && name.equals("length"))
            return ExpressionResolverImpl.ARRAY_LENGTH;

        for (final Clazz module : classpath) {
            if (Type.getType(module.getDescriptor()).equals(owner)) {
                final Field field = resolveFieldCtx(module, name);

                if (field == null)
                    continue;

                final Type type = resolveFieldType(module, name);

                if (type == null)
                    return null;

                return new FieldContext(field.getName(), module.getInternalName(), type, field.getAccessModifiers(), false);
            }
        }

        try {
            return resolveFieldCtx(resolveClass(owner.getClassName()), name);
        } catch (final ClassNotFoundException ignored) {
        }

        return null;
    }

    @Nullable
    private FieldContext resolveFieldCtx(@NotNull final Class<?> clazz, @NotNull final String name) {
        if (clazz.isArray() && name.equals("length"))
            return ExpressionResolverImpl.ARRAY_LENGTH;

        try {
            java.lang.reflect.Field f = clazz.getField(name);
            return new FieldContext(name, Type.getType(clazz).getInternalName(), Type.getType(f.getType()), f.getModifiers(), false);
        } catch (final NoSuchFieldException ignored) {
        }
        return null;
    }

    @Nullable
    public SymbolContext resolveFunctionContext(@NotNull final String name, @NotNull final Type... parameterTypes) {
        final Function function = resolveFunctionCtx(name, parameterTypes);

        if (function == null)
            return null;

        final Type type = resolveFunctionCtx(clazz, function);

        if (type == null)
            return null;

        return new SymbolContext(function.getName(), clazz.getInternalName(), type, function.getAccessModifiers());
    }

    private int selectFun(@NotNull final List<Type[]> functionArgList, @NotNull final Type... parameterTypes) {
        final MethodComparator mc = new MethodComparator(parameterTypes);

        final List<Type[]> types = new ArrayList<>(functionArgList);
        types.sort(mc);

        if (types.isEmpty())
            return -1;

        final Type[] bestTypes = types.get(0);

        for (int i = 0; i < bestTypes.length; i++) {
            if (!isAssignableFrom(parameterTypes[i], bestTypes[i]) &&
                    !isAssignableWithImplicitCast(parameterTypes[i], bestTypes[i]))
                return -1;
        }

        if (types.size() > 1) {
            if (mc.compare(bestTypes, types.get(1)) >= 0) {
                return -1;
            }
        }

        return functionArgList.indexOf(types.get(0));
    }

    private int selectExecutable(@NotNull final List<Executable> executables, @NotNull final Type... parameterTypes) {
        final LinkedList<Type[]> executableParamList = new LinkedList<>();

        for (final Executable executable : executables) {
            final Class<?>[] classes = executable.getParameterTypes();
            final Type[] types = new Type[classes.length];

            for (int i = 0; i < types.length; i++) {
                types[i] = Type.getType(classes[i]);
            }
            executableParamList.add(types);
        }

        return selectFun(executableParamList, parameterTypes);
    }

    @Nullable
    private Function resolveFunctionCtx(@NotNull final String name, @NotNull final Type... parameterTypes) {
        return resolveFunctionCtx(clazz, name, parameterTypes);
    }

    @Nullable
    private Function resolveFunctionCtx(@NotNull final Clazz clazz, @NotNull final String name, @NotNull final Type... parameterTypes) {
        final List<Type[]> functionTypeList = new LinkedList<>();
        final List<Function> functionList = new LinkedList<>();

        final List<Function> availableFunctions = new LinkedList<>(clazz.getFunctions());
        availableFunctions.addAll(clazz.getSyntheticFunctions());

        for (final Function function : availableFunctions) {
            final TypeName[] types = function.getParameterTypes();

            if (!function.getName().equals(name) || types.length != parameterTypes.length)
                continue;

            final Type type = resolveFunctionCtx(clazz, function);

            if (type != null) {
                functionTypeList.add(type.getArgumentTypes());
                functionList.add(function);
            }

        }

        int idx = selectFun(functionTypeList, parameterTypes);

        if (idx != -1)
            return functionList.get(idx);

        return null;
    }

    @Nullable
    public Type resolveType(@NotNull final Clazz clazz, final TypeName name) {
        final QualifiedName imp = getNameFromImports(clazz.getImports(), name);

        if (imp == null)
            return null;

        final Type type = resolveType(imp);

        if (type == null)
            return null;

        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < name.getDim(); i++) {
            builder.append("[");
        }

        builder.append(type.getDescriptor());

        return Type.getType(builder.toString());
    }

    private void defineParameters(final Function function) {
        final String[] parameterNames = function.getParameterNames();
        final List<Modifier>[] parameterModifiers = function.getParameterModifiers();
        final TypeName[] typeNames = function.getParameterTypes();

        for (int i = 0; i < function.getParameterNames().length; i++) {
            final Type type = typeNames[i].resolveType(this);
            final int modifiers = parameterModifiers[i].stream().mapToInt(Modifier::getValue).sum();
            scope.putVar(parameterNames[i], type, modifiers);
        }
    }

    @Nullable
    public Type resolveFunctionCtx(@NotNull final Clazz clazz, @NotNull final Function function) {
        final TypeName[] parameterTypes = function.getParameterTypes();
        final Type retType;

        if (function.isShortForm() && function.getType() == null) {
            if (function.getBody() instanceof Expression) {
                final Expression expr = (Expression) function.getBody();

                // must temporarily add function parameters into the scope
                // or else we cannot resolve them in the body
                scope.beginScope();
                defineParameters(function);
                retType = expr.resolveType(this);
                scope.endScope();
            } else {
                return null;
            }
        } else {
            retType = resolveType(clazz, function.getType());
        }

        final Type[] paramTypes = new Type[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            paramTypes[i] = resolveType(clazz, parameterTypes[i]);
            if (paramTypes[i] == null)
                return null;
        }

        if (retType == null)
            return null;

        return Type.getMethodType(retType, paramTypes);
    }

    private SymbolContext resolveCallFromStaticImports(@NotNull final String name, @NotNull final Type... parameterTypes) {
        for (final QualifiedName staticImp : clazz.getStaticImports()) {
            final Type type = resolveType(staticImp);

            if (type != null) {
                final SymbolContext ctx = resolveFunctionCtx(type, name, parameterTypes);

                if (ctx != null && ctx.isStatic())
                    return ctx;
            }
        }

        return null;
    }

    private FieldContext resolveFieldFromStaticImports(@NotNull final String name) {
        for (final QualifiedName staticImp : clazz.getStaticImports()) {
            final Type type = resolveType(staticImp);

            if (type != null) {
                final FieldContext ctx = resolveFieldCtx(type, name);

                if (ctx != null && ctx.isStatic())
                    return ctx;
            }
        }

        return null;
    }

    @Nullable
    private Field resolveFieldCtx(@NotNull final Clazz clazz, @NotNull final String name) {
        for (final Field field : clazz.getFields()) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    @Nullable
    private Type resolveFieldType(@NotNull final Clazz clazz, @NotNull final String name) {
        final Field field = resolveFieldCtx(clazz, name);

        if (field == null)
            return null;

        final TypeName fieldType = field.getType();

        if (fieldType == null) {
            Type type = field.getInitialValue().resolveType(this);

            if (type == null)
                return null;
            return type;
        }

        return resolveType(clazz, fieldType);
    }

    @Override
    public Type resolveBlock(@NotNull final Block block) {
        final BlockResolver blockResolver = new BlockResolver(this);
        return blockResolver.getType(block);
    }
}