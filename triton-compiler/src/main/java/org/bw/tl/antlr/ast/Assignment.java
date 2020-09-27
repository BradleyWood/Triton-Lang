package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;

@EqualsAndHashCode(callSuper = true)
public @Data class Assignment extends Expression {

    private final Expression precedingExpr;
    private final String name;
    private final Expression value;
    private boolean isDeclaration = false;

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return value.resolveType(resolver);
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitAssignment(this);
    }
}
