package org.bw.tl.antlr.ast;

import lombok.Data;

import java.util.List;

public @Data class File {

    private final QualifiedName packageName;
    private final List<QualifiedName> imports;
    private final List<Field> fields;
    private final List<Function> functions;
    private final String sourceFile;

}
