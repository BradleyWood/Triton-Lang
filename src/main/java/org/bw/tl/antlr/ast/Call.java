package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.bw.tl.compiler.types.Type;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public @Data class Call extends Expression {

    private final Expression precedingExpr;
    private final String name;
    private final List<Expression> parameters;

    public Call(final Expression precedingExpr, final String name, final Expression[] parameters) {
        this(precedingExpr, name, Arrays.asList(parameters));
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitCall(this);
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return resolver.resolveCall(this);
    }
}
