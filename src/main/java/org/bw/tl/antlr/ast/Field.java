package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data class Field extends ModifiableStatement {

    private final String name;
    private final Expression initialValue;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitField(this);
    }
}
