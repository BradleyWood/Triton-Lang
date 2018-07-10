package org.bw.tl.antlr.ast;

import lombok.Data;
import org.bw.tl.util.FileUtilities;
import org.bw.tl.util.TypeUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
    public Type resolveFunction(@NotNull final String name, @NotNull final Type... parameterTypes) {
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

                return getType(file, function.getType());
            }
        }

        return null;
    }

    @Nullable
    public Type resolveField(@NotNull final String name) {
        for (final File file : files) {
            for (final Field field : file.getFields()) {
                if (field.getName().equals(name)) {
                    return getType(file, field.getType());
                }
            }
        }
        return null;
    }
}
