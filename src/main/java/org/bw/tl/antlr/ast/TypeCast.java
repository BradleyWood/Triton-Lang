package org.bw.tl.antlr.ast;

import lombok.Data;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;

public @Data class TypeCast extends Expression {

    private final QualifiedName type;
    private final Expression expression;

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return null;
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitTypeCast(this);
    }
}
