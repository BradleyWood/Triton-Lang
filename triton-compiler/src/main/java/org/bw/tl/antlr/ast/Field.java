package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data class Field extends ModifiableStatement {

    private TypeName type;
    private final String name;
    private final Expression initialValue;

    private boolean isConstant = false;

    public Field(final String name, final TypeName type, final Expression initialValue) {
        this.name = name;
        this.type = type;
        this.initialValue = initialValue;
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitField(this);
    }
}
