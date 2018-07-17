package org.bw.tl.antlr.ast;

import lombok.Data;

import java.util.*;

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

    public String getModuleClassName() {
        if (modulePackage == null || modulePackage.length() == 0)
            return "default";
        return modulePackage.append(modulePackage.getNames()[modulePackage.length() - 1]).getName();
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
