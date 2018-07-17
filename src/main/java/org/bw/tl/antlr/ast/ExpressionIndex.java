package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;

import java.util.List;

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

        final char[] buf = expressionType.getDescriptor().toCharArray();

        int dim = 0;

        for (char ch : buf) {
            if (ch == '[')
                dim++;
        }

        if (indices.size() > dim || indices.isEmpty())
            return null;

        return Type.getType(expressionType.getDescriptor().substring(indices.size()));
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitExpressionIndices(this);
    }
}
