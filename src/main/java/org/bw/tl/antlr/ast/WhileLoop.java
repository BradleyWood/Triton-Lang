package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class WhileLoop extends Node {

    private final Expression condition;
    private final Node body;
    private final boolean doFirst;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitWhile(this);
    }
}
