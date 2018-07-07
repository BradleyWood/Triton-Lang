package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public @Data class Function extends ModifiableStatement {

    private final QualifiedName[] parameterTypes;
    private final String[] parameterNames;
    private final String name;
    private final Block body;
    private final QualifiedName type;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitFunction(this);
    }
}
