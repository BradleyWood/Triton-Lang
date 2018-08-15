package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data class Return extends Node {

    private final Expression expression;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitReturn(this);
    }
}
