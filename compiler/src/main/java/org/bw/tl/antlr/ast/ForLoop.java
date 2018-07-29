package org.bw.tl.antlr.ast;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public @Data class ForLoop extends Node {

    private final ForControl forControl;
    private final Node body;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitFor(this);
    }
}
