package org.bw.tl.antlr.ast;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

public abstract @Data class Node {

    private int lineNumber = -1;

    @Nullable
    private String text;

    @Nullable
    private String file;

    @Nullable
    private Node parent;

    public abstract void accept(final ASTVisitor visitor);

}
