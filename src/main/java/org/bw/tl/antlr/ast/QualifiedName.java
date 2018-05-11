package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = false)
public @Data class QualifiedName extends Expression {

    @NotNull
    private final String[] names;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitName(this);
    }
}
