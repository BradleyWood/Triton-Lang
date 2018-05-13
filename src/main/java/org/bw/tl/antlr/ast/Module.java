package org.bw.tl.antlr.ast;

import lombok.Data;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
}
