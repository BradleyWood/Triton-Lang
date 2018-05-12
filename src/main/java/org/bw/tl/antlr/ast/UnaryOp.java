package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public @Data class UnaryOp extends Expression {

    private final @Getter Expression expression;
    private final @Getter String operator;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitUnaryOp(this);
    }
}
