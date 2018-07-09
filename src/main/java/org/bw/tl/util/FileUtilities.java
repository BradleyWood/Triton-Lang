package org.bw.tl.util;

import org.bw.tl.antlr.ast.File;
import org.bw.tl.antlr.ast.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import static org.bw.tl.util.TypeUtilities.getTypeFromName;

public class FileUtilities {

    /**
     * Resolves the type information from the given name. Assumes
     * that the imports are valid and exist
     *
     * @param ctx  the file where the type is used
     * @param name the name to resolve
     * @return the type if it exists, otherwise null
     */
    @Nullable
    public static Type getType(@NotNull final File ctx, @NotNull final QualifiedName name) {
        for (final QualifiedName imp : ctx.getImports()) {
            if (name.length() == 1 && imp.endsWith(name.toString()) || imp.equals(name)) {
                return Type.getType(imp.getDesc());
            }
        }
        return getTypeFromName(name);
    }
}
