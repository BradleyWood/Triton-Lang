package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
public @Data class ForEachLoop extends Node {

    private final @NotNull Field field;
    private final @NotNull Expression iterableExpression;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitForEach(this);
    }
}
