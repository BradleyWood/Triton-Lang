package org.triton.antlr.ast;

import lombok.Data;
import org.bw.tl.antlr.ast.Node;
import org.bw.tl.antlr.ast.QualifiedName;

import java.util.List;

public @Data class Script {

    private final List<QualifiedName> imports;
    private final List<Node> statements;
    private final String srcFile;
}
