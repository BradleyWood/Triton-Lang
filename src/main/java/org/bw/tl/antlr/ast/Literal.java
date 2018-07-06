package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;

@EqualsAndHashCode(callSuper = true)
public @Data class Literal<T> extends Expression {

    private final T value;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitLiteral(this);
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return resolver.resolveLiteral(this);
    }
}
