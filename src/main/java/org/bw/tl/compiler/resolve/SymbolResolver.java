package org.bw.tl.compiler.resolve;

import lombok.Data;
import lombok.AllArgsConstructor;
import org.bw.tl.antlr.ast.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.bw.tl.util.TypeUtilities.*;

@AllArgsConstructor
public @Data class SymbolResolver {

    /**
     * All classpath in the classpath
     */
    @NotNull
    private final List<Module> classpath;

    /**
     * The local module that is attempting to make a resolution
     */
    @NotNull
    private Module ctx;

    @NotNull
    private File file;

    @Nullable
    public SymbolContext resolveFunction(@NotNull final Type owner, @NotNull final String name, @NotNull final Type... parameterTypes) {
        for (final Module module : classpath) {
            if (module.getModulePackage().getName().equals(owner.getClassName())) {
                final Function fun = resolveFunction(module, name, parameterTypes);
                if (fun != null) {
                    final Type retType = getTypeFromName(fun.getType());

                    if (retType == null)
                        return null;

                    final Type methodType = resolveFunctionType(module, fun);

                    if (methodType == null)
                        return null;

                    return new SymbolContext(fun.getName(), module.getInternalName(), methodType, fun.getAccessModifiers());
                }
            }
        }

        try {
            return resolveFunction(Class.forName(owner.getClassName()), name, parameterTypes);
        } catch (ClassNotFoundException ignored) {
        }

        return null;
    }

    @Nullable
    public SymbolContext resolveFunction(final @NotNull Class<?> clazz, final @NotNull String name,
                                         final boolean exactParams, @NotNull final Type... parameterTypes) {
        next:
        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                final Class<?>[] types = method.getParameterTypes();

                if (types.length != parameterTypes.length)
                    continue;

                for (int i = 0; i < types.length; i++) {
                    final Type funParamType = Type.getType(types[i]);

                    if (!parameterTypes[i].equals(funParamType) && exactParams) {
                        continue next;
                    } else if (!parameterTypes[i].equals(funParamType)) {
                        if (!isAssignableFrom(parameterTypes[i], funParamType)
                                && !isAssignableWithImplicitCast(parameterTypes[i], funParamType)) {
                            continue next;
                        }
                    }
                }

                return new SymbolContext(name, clazz.getName().replace(".", "/"), Type.getType(method),
                        method.getModifiers());
            }
        }
        return null;
    }

    @Nullable
    public SymbolContext resolveFunction(final @NotNull Class<?> clazz, final @NotNull String name,
                                         @NotNull final Type... parameterTypes) {
        final SymbolContext ctx = resolveFunction(clazz, name, true, parameterTypes);

        if (ctx != null)
            return ctx;

        return resolveFunction(clazz, name, false, parameterTypes);
    }

    @Nullable
    public Type resolveType(@NotNull final QualifiedName name) {
        if (name.length() == 0)
            return null;

        QualifiedName typeName = name;

        for (final QualifiedName imp : file.getImports()) {
            if (imp.endsWith(name.getNames()[0])) {
                typeName = imp;
            }
        }

        for (final Module module : classpath) {
            if (module.getModulePackage().getName().equals(typeName.getName())) {
                return Type.getType(module.getModulePackage().getDesc());
            }
        }

        try {
            return Type.getType(Class.forName(typeName.getName()));
        } catch (ClassNotFoundException ignored) {
        }

        return getTypeFromName(name);
    }

    @Nullable
    public SymbolContext resolveConstructor(@NotNull final QualifiedName owner, @NotNull final Type... parameterTypes) {
        final Type type = resolveType(owner);

        if (type != null)
            return resolveConstructor(type, parameterTypes);

        return null;
    }

    @Nullable
    public SymbolContext resolveConstructor(@NotNull final Type owner, @NotNull final Type... parameterTypes) {
        final SymbolContext ctx = resolveConstructor(owner, true, parameterTypes);

        if (ctx == null)
            return resolveConstructor(owner, false, parameterTypes);

        return ctx;
    }

    @Nullable
    public SymbolContext resolveConstructor(@NotNull final Type owner, final boolean exactParams,
                                            @NotNull final Type... parameterTypes) {
        try {
            final Class<?> clazz = Class.forName(owner.getClassName());

            next:
            for (final Constructor<?> constructor : clazz.getConstructors()) {
                final Class<?>[] types = constructor.getParameterTypes();

                if (types.length != parameterTypes.length)
                    continue;

                for (int i = 0; i < types.length; i++) {
                    final Type funParamType = Type.getType(types[i]);

                    if (!parameterTypes[i].equals(funParamType) && exactParams) {
                        continue next;
                    } else if (!parameterTypes[i].equals(funParamType)) {
                        if (!isAssignableFrom(parameterTypes[i], funParamType)
                                && !isAssignableWithImplicitCast(parameterTypes[i], funParamType)) {
                            continue next;
                        }
                    }
                }

                return new SymbolContext("<init>", owner.getInternalName(), Type.getType(constructor),
                        constructor.getModifiers());
            }
        } catch (final ClassNotFoundException ignored) {
        }

        return null;
    }

    @Nullable
    public FieldContext resolveField(@NotNull final Type owner, @NotNull final String name) {
        for (final Module module : classpath) {
            if (module.getModulePackage().getName().equals(owner.getClassName())) {
                final Field field = resolveField(module, name);

                if (field == null)
                    continue;

                final Type type = resolveFieldType(module, name);

                if (type == null)
                    return null;

                return new FieldContext(field.getName(), module.getInternalName(), type, field.getAccessModifiers(), false);
            }
        }

        try {
            return resolveField(Class.forName(owner.getClassName()), name);
        } catch (final ClassNotFoundException ignored) {
        }

        return null;
    }

    @Nullable
    public QualifiedName getImportFromName(@NotNull final String name) {
        for (final QualifiedName qualifiedName : file.getImports()) {
            if (qualifiedName.endsWith(name))
                return qualifiedName;
        }

        return null;
    }

    @Nullable
    public FieldContext resolveField(@NotNull final Class<?> clazz, @NotNull final QualifiedName name) {
        final String[] names = name.getNames();

        if (names.length == 0)
            return null;

        Class<?> cl = clazz;

        for (int i = 0; i < names.length && cl != null; i++) {
            try {
                cl = cl.getField(names[i]).getType();
            } catch (final Throwable ignored) {
                return null;
            }
        }

        if (cl != null) {
            return new FieldContext(names[names.length - 1], Type.getType(cl).getInternalName(), Type.getType(cl), cl.getModifiers(), false);
        }

        return null;
    }

    @Nullable
    public FieldContext resolveField(@NotNull final Class<?> clazz, @NotNull final String name) {
        try {
            java.lang.reflect.Field f = clazz.getDeclaredField(name);
            return new FieldContext(name, Type.getType(clazz).getInternalName(), Type.getType(f.getType()), f.getModifiers(), false);
        } catch (final NoSuchFieldException ignored) {
        }
        return null;
    }

    @Nullable
    public FieldContext resolveField(@NotNull final QualifiedName name) {
        final String[] names = name.getNames();

        if (names.length == 0)
            return null;

        if (names.length == 1) {
            final Field field = resolveField(ctx, name.getName());

            if (field == null)
                return null;

            final Type type = resolveType(field.getType());

            if (type == null)
                return null;

            return new FieldContext(field.getName(), ctx.getInternalName(), type, field.getAccessModifiers(), false);
        }

        outer:
        for (final QualifiedName imp : file.getImports()) {
            QualifiedName fqn = imp;
            int idx = fqn.length();

            if (name.equals(imp)) // name is fqn not a field
                return null;

            for (final String n : names) {
                if (fqn.beginsWith(n)) {
                    fqn = fqn.subname(1, fqn.length());
                } else if (fqn.endsWith(n)) {
                    idx = 1;
                    break;
                } else continue outer;
            }

            final Type type = resolveType(imp);

            if (type == null) {
                try {
                    return resolveField(Class.forName(imp.getName()), name.subname(idx, name.length()));
                } catch (final ClassNotFoundException ignored) {
                    ignored.printStackTrace();
                }
                break;
            }

            FieldContext fieldType = resolveField(type, names[idx]);

            for (int i = idx + 1; i < names.length && fieldType != null; i++) {
                fieldType = resolveField(fieldType.getTypeDescriptor(), names[i]);
            }

            return fieldType;
        }

        try {
            final Class<?> clazz = Class.forName(name.subname(0, name.length() - 1).getName());
            return resolveField(clazz, names[names.length - 1]);
        } catch (final ClassNotFoundException ignored) {
        }
        return null;
    }

    @Nullable
    public Function resolveFunction(@NotNull final Module module, @NotNull final String name, @NotNull final Type... parameterTypes) {
        final Function fun = resolveFunction(module, name, true, parameterTypes);

        if (fun != null)
            return fun;

        return resolveFunction(module, name, false, parameterTypes);
    }

    @Nullable
    public SymbolContext resolveFunctionContext(@NotNull final Module module, @NotNull final String name, @NotNull final Type... parameterTypes) {
        final Function function = resolveFunction(module, name, parameterTypes);

        if (function == null)
            return null;

        resolveFunctionType(module, name, parameterTypes);
        final Type type = resolveFunctionType(module, function);

        if (type == null)
            return null;

        return new SymbolContext(function.getName(), module.getInternalName(), type, function.getAccessModifiers());
    }

    @Nullable
    public Function resolveFunction(@NotNull final Module module, @NotNull final String name, final boolean exactParams,
                                    @NotNull final Type... parameterTypes) {
        for (final File file : module.getFiles()) {
            fun:
            for (final Function function : file.getFunctions()) {
                final QualifiedName[] types = function.getParameterTypes();

                if (types.length != parameterTypes.length || !function.getName().equals(name))
                    continue;


                for (int i = 0; i < types.length; i++) {
                    final Type ti = resolveType(file, types[i]);

                    if (ti == null)
                        return null;

                    if (!parameterTypes[i].equals(ti) && exactParams) {
                        continue fun;
                    } else if (!parameterTypes[i].equals(ti)) {
                        if (!isAssignableFrom(parameterTypes[i], ti) && !isAssignableWithImplicitCast(parameterTypes[i], ti))
                            continue fun;
                    }

                }

                return function;
            }
        }

        return null;
    }

    @Nullable
    public Type resolveFunctionType(@NotNull final Module module, @NotNull final Function function) {
        final Optional<File> file = module.getFiles().stream().filter(f -> f.getFunctions().contains(function)).findFirst();

        return file.map(f -> resolveFunction(f, function)).orElse(null);
    }

    @Nullable
    public Type resolveType(@NotNull final File file, final QualifiedName name) {
        final QualifiedName imp = getNameFromImports(file.getImports(), name);

        if (imp == null)
            return null;

        return resolveType(name);
    }

    @Nullable
    public Type resolveFunction(@NotNull final File file, @NotNull final Function function) {
        final QualifiedName[] parameterTypes = function.getParameterTypes();
        final Type retType = resolveType(file, function.getType());
        final Type[] paramTypes = new Type[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            paramTypes[i] = resolveType(file, parameterTypes[i]);
            if (paramTypes[i] == null)
                return null;
        }

        if (retType == null)
            return null;

        return Type.getMethodType(retType, paramTypes);
    }

    @Nullable
    public Type resolveFunctionType(@NotNull final Module module, @NotNull final String name, @NotNull final Type... parameterTypes) {
        final Function fun = resolveFunction(module, name, parameterTypes);

        if (fun == null)
            return null;

        return resolveFunctionType(module, fun);
    }

    @Nullable
    public Type resolveFunctionReturnType(@NotNull final Module module, @NotNull final String name, @NotNull final Type... parameterTypes) {
        final Function function = resolveFunction(module, name, parameterTypes);

        if (function == null)
            return null;

        final Optional<File> file = module.getFiles().stream().filter(f -> f.getFunctions().contains(function)).findFirst();

        return file.map(f -> resolveType(f, function.getType())).orElse(null);
    }

    @Nullable
    public Field resolveField(@NotNull final Module module, @NotNull final String name) {
        for (final File file : module.getFiles()) {
            for (final Field field : file.getFields()) {
                if (field.getName().equals(name)) {
                    return field;
                }
            }
        }
        return null;
    }

    @Nullable
    public Type resolveFieldType(@NotNull final Module module, @NotNull final String name) {
        final Field field = resolveField(module, name);

        if (field == null)
            return null;

        final Optional<File> file = module.getFiles().stream().filter(f -> f.getFields().contains(field)).findFirst();

        return file.map(f -> resolveType(f, field.getType())).orElse(null);
    }
}
