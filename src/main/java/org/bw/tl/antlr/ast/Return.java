package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public @Data class Return extends Node {

    private final @Getter Expression expression;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitReturn(this);
    }
}
