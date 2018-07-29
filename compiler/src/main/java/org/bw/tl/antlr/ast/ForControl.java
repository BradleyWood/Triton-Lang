package org.bw.tl.antlr.ast;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public @Data class ForControl {

    @Nullable
    private final Node init;
    @Nullable
    private final Expression condition;
    @NotNull
    private final List<Expression> update;

}
