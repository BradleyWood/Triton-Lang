package org.bw.tl.compiler.resolve;

import lombok.Data;
import lombok.AllArgsConstructor;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.types.Primitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.bw.tl.util.TypeUtilities.*;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@AllArgsConstructor
public @Data class SymbolResolver {

    public static FieldContext ARRAY_LENGTH = new FieldContext("length", "java/lang/Object",
            Type.INT_TYPE, ACC_PUBLIC, false);

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
    public SymbolContext resolveFunction(final @NotNull Class<?> clazz, @NotNull final String name, @NotNull final Type... parameterTypes) {
        final List<Executable> candidates = new LinkedList<>();

        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                final Class<?>[] types = method.getParameterTypes();

                if (!method.getName().equals(name) || types.length != parameterTypes.length)
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

    private int rateParameters(final Type[] parameterTypes, final Type[] requiredTypes) {
        if (parameterTypes == null)
            return -1;

        final List<Type> DISCRETE_TYPES = Arrays.asList(Type.BYTE_TYPE, Type.SHORT_TYPE, Type.INT_TYPE, Type.LONG_TYPE);
        final List<Type> CONTINUOUS_TYPES = Arrays.asList(Type.FLOAT_TYPE, Type.DOUBLE_TYPE);

        int rating = 0;

        for (int i = 0; i < parameterTypes.length; i++) {
            if (!parameterTypes[i].equals(requiredTypes[i])) {
                if (DISCRETE_TYPES.contains(parameterTypes[i]) && DISCRETE_TYPES.contains(requiredTypes[i])) {
                    if (DISCRETE_TYPES.indexOf(requiredTypes[i]) < DISCRETE_TYPES.indexOf(parameterTypes[i]))
                        return -1;
                } else if (CONTINUOUS_TYPES.contains(parameterTypes[i]) && CONTINUOUS_TYPES.contains(requiredTypes[i])) {
                    if (!parameterTypes[i].equals(requiredTypes[i]) && requiredTypes[i] == Type.FLOAT_TYPE)
                        return -1;
                } else {
                    final Primitive parameterType = Primitive.getPrimitiveByDesc(parameterTypes[i].getDescriptor());
                    final Primitive requiredType = Primitive.getPrimitiveByDesc(requiredTypes[i].getDescriptor());

                    if ((parameterType == null || parameterType != requiredType) && (parameterType == null && requiredType == null)) {
                        if (!isAssignableFrom(parameterTypes[i], requiredTypes[i])) {
                            if (isAssignableWithImplicitCast(parameterTypes[i], requiredTypes[i])) {
                                rating += 2;
                            } else {
                                return -1;
                            }
                        }
                    } else {
                        return -1;
                    }
                }
            }
        }

        return rating;
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
        try {
            final Class<?> clazz = Class.forName(owner.getClassName());
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
    public FieldContext resolveField(@NotNull final Type owner, @NotNull final String name) {
        if (owner.getDescriptor().startsWith("[") && name.equals("length"))
            return ARRAY_LENGTH;

        for (final Module module : classpath) {
            if (Type.getType(module.getDescriptor()).equals(owner)) {
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
    public FieldContext resolveField(@NotNull final Class<?> clazz, @NotNull final String name) {
        if (clazz.isArray() && name.equals("length"))
            return ARRAY_LENGTH;

        try {
            java.lang.reflect.Field f = clazz.getField(name);
            return new FieldContext(name, Type.getType(clazz).getInternalName(), Type.getType(f.getType()), f.getModifiers(), false);
        } catch (final NoSuchFieldException ignored) {
        }
        return null;
    }

    @Nullable
    public SymbolContext resolveFunctionContext(@NotNull final Module module, @NotNull final String name, @NotNull final Type... parameterTypes) {
        final Function function = resolveFunction(module, name, parameterTypes);

        if (function == null)
            return null;

        final Type type = resolveFunctionType(module, function);

        if (type == null)
            return null;

        return new SymbolContext(function.getName(), module.getInternalName(), type, function.getAccessModifiers());
    }

    public int selectFun(@NotNull final List<Type[]> functionArgList, @NotNull final Type... parameterTypes) {
        final int[] ratings = new int[functionArgList.size()];

        for (int i = 0; i < ratings.length; i++) {
            ratings[i] = rateParameters(parameterTypes, functionArgList.get(i));
        }

        int best = -1;

        for (int i = 0; i < ratings.length; i++) {
            if (ratings[i] != -1 && (best == -1 || ratings[i] < ratings[best]))
                best = i;
        }

        return best;
    }

    public int selectExecutable(@NotNull final List<Executable> executables, @NotNull final Type... parameterTypes) {
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
    public Function resolveFunction(@NotNull final Module module, @NotNull final String name, @NotNull final Type... parameterTypes) {
        final List<Type[]> functionTypeList = new LinkedList<>();
        final List<Function> functionList = new LinkedList<>();

        for (final File file : module.getFiles()) {
            for (final Function function : file.getFunctions()) {
                final TypeName[] types = function.getParameterTypes();

                if (!function.getName().equals(name) || types.length != parameterTypes.length)
                    continue;

                final Type type = resolveFunction(file, function);

                if (type != null) {
                    functionTypeList.add(type.getArgumentTypes());
                    functionList.add(function);
                }
            }
        }

        int idx = selectFun(functionTypeList, parameterTypes);

        if (idx != -1)
            return functionList.get(idx);

        return null;
    }

    @Nullable
    public Type resolveFunctionType(@NotNull final Module module, @NotNull final Function function) {
        final Optional<File> file = module.getFiles().stream().filter(f -> f.getFunctions().contains(function)).findFirst();

        return file.map(f -> resolveFunction(f, function)).orElse(null);
    }

    @Nullable
    public Type resolveType(@NotNull final File file, final TypeName name) {
        final QualifiedName imp = getNameFromImports(file.getImports(), name);

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

    @Nullable
    public Type resolveFunction(@NotNull final File file, @NotNull final Function function) {
        final TypeName[] parameterTypes = function.getParameterTypes();
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
