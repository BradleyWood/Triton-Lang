package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.bw.tl.compiler.types.Type;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = false)
public @Data class QualifiedName extends Expression {

    @NotNull
    private final String[] names;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitName(this);
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return resolver.resolveName(this);
    }
}
