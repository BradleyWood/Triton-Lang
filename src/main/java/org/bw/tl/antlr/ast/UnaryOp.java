package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.bw.tl.compiler.types.Type;

@EqualsAndHashCode(callSuper = true)
public @Data class UnaryOp extends Expression {

    private final Expression expression;
    private final String operator;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitUnaryOp(this);
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return resolver.resolveUnaryOp(this);
    }
}
