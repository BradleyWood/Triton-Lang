package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data class RmdAsyncDelegate extends Node {

    private final Block body;
    private final Block callback;
    private final Expression condition;
    private String inputVar = "it";

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitRmdAsyncDelegate(this);
    }
}
