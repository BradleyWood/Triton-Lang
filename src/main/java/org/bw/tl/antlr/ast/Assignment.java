package org.bw.tl.antlr.ast;

import lombok.Data;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;

public @Data class Assignment extends Expression {

    private final Expression precedingExpr;
    private final String name;
    private final Expression value;

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return value.resolveType(resolver);
    }

    @Override
    public void accept(final ASTVisitor visitor) {

    }
}
