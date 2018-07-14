package org.bw.tl.antlr.ast;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.*;

import static org.bw.tl.util.FileUtilities.getType;
import static org.bw.tl.util.TypeUtilities.isAssignableFrom;
import static org.bw.tl.util.TypeUtilities.isAssignableWithImplicitCast;

public @Data class Module {

    private final QualifiedName modulePackage;
    private final List<File> files;

    /**
     * Creates the representation of a module
     *
     * @param files The files that make up the module
     * @return The module, if valid
     */
    public static Module of(final List<File> files) {
        if (files == null || files.isEmpty())
            return null;

        final Iterator<File> it = files.iterator();
        final QualifiedName packageName = it.next().getPackageName();

        while (it.hasNext()) {
            if (!Objects.equals(packageName, it.next().getPackageName()))
                return null;
        }

        return new Module(packageName, files);
    }

    public static Module of(final File... files) {
        return of(Arrays.asList(files));
    }

    @Nullable
    public Function resolveFunction(@NotNull final String name, @NotNull final Type... parameterTypes) {
        for (final File file : files) {
            fun:
            for (final Function function : file.getFunctions()) {
                final QualifiedName[] types = function.getParameterTypes();

                if (types.length != parameterTypes.length || !function.getName().equals(name))
                    continue;

                for (int i = 0; i < types.length; i++) {
                    final Type ti = getType(file, types[i]);
                    if (!parameterTypes[i].equals(ti) && !isAssignableFrom(parameterTypes[i], ti)
                            && !isAssignableWithImplicitCast(parameterTypes[i], ti))
                        break fun;
                }

                return function;
            }
        }

        return null;
    }

    @Nullable
    public Type resolveFunctionType(@NotNull final String name, @NotNull final Type... parameterTypes) {
        final Function function = resolveFunction(name, parameterTypes);

        if (function == null)
            return null;

        final Optional<File> file = files.stream().filter(f -> f.getFunctions().contains(function)).findFirst();

        return file.map(f -> getType(f, function.getType())).orElse(null);
    }

    @Nullable
    public Field resolveField(@NotNull final String name) {
        for (final File file : files) {
            for (final Field field : file.getFields()) {
                if (field.getName().equals(name)) {
                    return field;
                }
            }
        }
        return null;
    }

    @Nullable
    public Type resolveFieldType(@NotNull final String name) {
        final Field field = resolveField(name);

        if (field == null)
            return null;

        final Optional<File> file = files.stream().filter(f -> f.getFields().contains(field)).findFirst();

        return file.map(f -> getType(f, field.getType())).orElse(null);
    }

    public String getModuleClassName() {
        if (modulePackage == null || modulePackage.length() == 0)
            return "default";
        return modulePackage.append(modulePackage.getNames()[modulePackage.length() - 1]).toString();
    }

    public String getInternalName() {
        return getModuleClassName().replace(".", "/");
    }

    public String getDescriptor() {
        return "L" + getInternalName() + ";";
    }

    /**
     * Creates a list containing all fields in the module.
     * Adding or removing from this list doest not modify the module.
     *
     * @return A list containing all fields in the module
     */
    public List<Field> getFields() {
        final List<Field> fields = new LinkedList<>();
        files.forEach(file -> fields.addAll(file.getFields()));
        return fields;
    }

    /**
     * Creates a list containing all the functions in the module.
     * Adding or removing from this list does not modify the module.
     *
     * @return A list containing all functions in the module
     */
    public List<Function> getFunctions() {
        final List<Function> fields = new LinkedList<>();
        files.forEach(file -> fields.addAll(file.getFunctions()));
        return fields;
    }
}
