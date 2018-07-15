package org.bw.tl.antlr.ast;

import lombok.Data;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;

import java.util.List;

public @Data class New extends Expression {

    private final QualifiedName type;
    private final List<Expression> parameters;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitNew(this);
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return resolver.resolveConstructor(this);
    }
}
