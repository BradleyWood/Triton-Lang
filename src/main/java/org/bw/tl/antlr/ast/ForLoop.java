package org.bw.tl.antlr.ast;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public @Data class ForLoop extends Node {

    private final Node init;
    private final Expression condition;
    private final Node body;
    private final List<Expression> update;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitFor(this);
    }
}
