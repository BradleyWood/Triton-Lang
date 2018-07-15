package org.bw.tl.compiler.resolve;

import lombok.Data;
import lombok.AllArgsConstructor;
import org.bw.tl.antlr.ast.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.List;

import static org.bw.tl.util.TypeUtilities.getTypeFromName;
import static org.bw.tl.util.TypeUtilities.isAssignableFrom;
import static org.bw.tl.util.TypeUtilities.isAssignableWithImplicitCast;

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
    public SymbolContext resolveFunction(@NotNull Type owner, @NotNull String name, @NotNull final Type... parameterTypes) {
        for (final Module module : classpath) {
            if (module.getModulePackage().toString().equals(owner.getClassName())) {
                final Function fun = module.resolveFunction(name, parameterTypes);
                if (fun != null) {
                    final Type retType = getTypeFromName(fun.getType());

                    if (retType == null)
                        return null;

                    final Type methodType = module.resolveFunctionType(fun);
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
                                         @NotNull final Type... parameterTypes) {
        next:
        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                final Class<?>[] types = method.getParameterTypes();
                for (int i = 0; i < types.length; i++) {
                    final Type funParamType = Type.getType(types[i]);
                    if (!parameterTypes[i].equals(funParamType) && !isAssignableFrom(parameterTypes[i], funParamType)
                            && !isAssignableWithImplicitCast(parameterTypes[i], funParamType)) {
                        break next;
                    }
                }
                return new SymbolContext(name, clazz.getName().replace(".", "/"), Type.getType(method),
                        method.getModifiers());
            }
        }
        return null;
    }

    @Nullable
    public Type resolveType(@NotNull final QualifiedName name) {
        for (final Module module : classpath) {
            if (module.getModulePackage().toString().equals(name.toString())) {
                return Type.getType(module.getModulePackage().getDesc());
            }
        }

        try {
            return Type.getType(Class.forName(name.toString()));
        } catch (ClassNotFoundException ignored) {
        }

        return getTypeFromName(name);
    }

    @Nullable
    public FieldContext resolveField(@NotNull final Type owner, @NotNull final String name) {
        for (final Module module : classpath) {
            if (module.getModulePackage().toString().equals(owner.getClassName())) {
                final Field field = module.resolveField(name);

                if (field == null)
                    continue;

                final Type type = module.resolveFieldType(name);

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
            final Field field = ctx.resolveField(name.toString());

            if (field == null)
                return null;

            return new FieldContext(field.getName(), ctx.getInternalName(), getTypeFromName(field.getType()), field.getAccessModifiers(), false);
        }

        outer:
        for (final QualifiedName imp : file.getImports()) {
            QualifiedName fqn = imp;
            int idx = fqn.length();

            for (final String n : names) {
                if (fqn.beginsWith(n)) {
                    fqn = fqn.subname(1, fqn.length());
                } else if (fqn.endsWith(n)) {
                    idx = 1;
                    break;
                } else break outer;
            }

            final Type type = resolveType(imp);

            if (type == null)
                break;

            FieldContext fieldType = resolveField(type, names[idx]);

            for (int i = idx + 1; i < names.length && fieldType != null; i++) {
                fieldType = resolveField(fieldType.getTypeDescriptor(), names[i]);
            }

            return fieldType;
        }

        try {
            final Class<?> clazz = Class.forName(name.subname(0, name.length() - 1).toString());
            return resolveField(clazz, names[names.length - 1]);
        } catch (final ClassNotFoundException ignored) {
        }
        return null;
    }
}
