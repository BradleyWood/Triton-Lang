package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;

import java.util.List;

import static org.bw.tl.util.TypeUtilities.getDim;

@EqualsAndHashCode(callSuper = true)
public @Data class ExpressionIndex extends Expression {

    private final Expression expression;
    private final List<Expression> indices;
    private final Expression value;

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        final Type expressionType = expression.resolveType(resolver);
        if (expressionType == null)
            return null;

        if (indices.size() > getDim(expressionType) || indices.isEmpty())
            return null;

        return Type.getType(expressionType.getDescriptor().substring(indices.size()));
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitExpressionIndices(this);
    }
}
