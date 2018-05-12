package org.bw.tl.antlr.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class File {

    private final @Getter QualifiedName packageName;
    private final @Getter List<QualifiedName> imports;
    private final @Getter List<Field> fields;
    private final @Getter List<Function> functions;

}
