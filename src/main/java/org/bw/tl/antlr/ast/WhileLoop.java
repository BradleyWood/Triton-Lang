package org.bw.tl.antlr.ast;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WhileLoop extends Node {

    private final @Getter Expression condition;
    private final @Getter Node body;
    private final @Getter boolean doFirst;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitWhile(this);
    }
}
