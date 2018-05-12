package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public @Data class BinaryOp extends Expression {

    private final @Getter Expression leftSide;
    private final @Getter String operator;
    private final @Getter Expression rightSide;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitBinaryOp(this);
    }
}
