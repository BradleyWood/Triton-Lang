package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data class UnaryOp extends Expression {

    private final Expression expression;
    private final String operator;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitUnaryOp(this);
    }
}
