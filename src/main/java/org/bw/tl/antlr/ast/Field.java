package org.bw.tl.antlr.ast;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Field extends ModifiableStatement {

    private final @Getter String name;
    private final @Getter Expression initialValue;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitField(this);
    }
}
