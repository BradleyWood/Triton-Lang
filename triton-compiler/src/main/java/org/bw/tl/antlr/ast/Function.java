package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public @Data class Function extends ModifiableStatement {

    private final TypeName[] parameterTypes;
    private final String[] parameterNames;
    private final List<Modifier>[] parameterModifiers;
    private final String name;
    private final Node body;
    private final TypeName type;
    private boolean shortForm;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitFunction(this);
    }
}
