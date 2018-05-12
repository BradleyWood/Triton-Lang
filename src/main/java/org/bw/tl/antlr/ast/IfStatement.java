package org.bw.tl.antlr.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public @Data class IfStatement extends Node {

    private final @Getter Expression condition;
    private final @Getter Node body;
    private final @Getter Node elseBody;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitIf(this);
    }
}
