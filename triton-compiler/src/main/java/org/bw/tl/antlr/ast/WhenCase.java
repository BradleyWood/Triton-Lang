package org.bw.tl.antlr.ast;

import lombok.Data;

public @Data class WhenCase {

    private final Expression condition;
    private final Node branch;
}
