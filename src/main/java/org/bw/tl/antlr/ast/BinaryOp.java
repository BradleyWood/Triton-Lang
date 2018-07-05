package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.bw.tl.compiler.types.Type;

@EqualsAndHashCode(callSuper = true)
public @Data class BinaryOp extends Expression {

    private final Expression leftSide;
    private final String operator;
    private final Expression rightSide;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitBinaryOp(this);
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return resolver.resolveBinaryOp(this);
    }
}
