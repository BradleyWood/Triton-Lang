package org.bw.tl.antlr.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public @Data class Block extends Node {

    private final List<Node> statements;

    public Block(final Node[] statements) {
        this(new ArrayList<>(Arrays.asList(statements)));
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        statements.forEach(stmt -> stmt.accept(visitor));
    }
}
