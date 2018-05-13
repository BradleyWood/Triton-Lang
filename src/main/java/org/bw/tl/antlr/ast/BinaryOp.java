package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data class BinaryOp extends Expression {

    private final Expression leftSide;
    private final String operator;
    private final Expression rightSide;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitBinaryOp(this);
    }
}
