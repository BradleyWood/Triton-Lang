package org.bw.tl.antlr.ast;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public @Data class Clazz {

    private final List<Function> syntheticFunctions = new LinkedList<>();
    private final QualifiedName packageName;
    private final List<QualifiedName> imports;
    private final List<QualifiedName> staticImports;
    private final List<Field> fields;
    private final List<Function> functions;
    private final List<ScheduleBlock> scheduleBlocks;
    private final String sourceFile;

    public String getName() {
        final java.io.File file = new java.io.File(sourceFile);
        final String name = file.getName();

        int idx = name.indexOf('.');

        if (idx != -1)
            return name.substring(0, idx);

        return name;
    }

    public void addSyntheticFunction(final Function function) {
        syntheticFunctions.add(function);
    }

    public String getModuleClassName() {
        if (packageName == null || packageName.length() == 0)
            return "default.";
        return packageName.append(getName()).getName();
    }

    public String getInternalName() {
        return getModuleClassName().replace(".", "/");
    }

    public String getDescriptor() {
        return "L" + getInternalName() + ";";
    }

}
