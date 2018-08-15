package org.bw.tl.antlr.ast;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public @Data class ForLoop extends Node {

    @Nullable
    private final Node init;
    @Nullable
    private final Expression condition;
    @NotNull
    private final List<Expression> update;

    private final Node body;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitFor(this);
    }
}
