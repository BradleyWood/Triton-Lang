package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public @Data class Literal<T> extends Expression {

    private final @Getter T value;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitLiteral(this);
    }

}
