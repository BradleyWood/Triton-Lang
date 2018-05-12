package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public @Data class Function extends ModifiableStatement {

    private final @Getter String name;
    private final @Getter Block body;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitFunction(this);
    }
}
