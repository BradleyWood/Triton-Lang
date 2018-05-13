package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public @Data class IfStatement extends Node {

    private final Expression condition;
    private final Node body;
    private final Node elseBody;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitIf(this);
    }
}
