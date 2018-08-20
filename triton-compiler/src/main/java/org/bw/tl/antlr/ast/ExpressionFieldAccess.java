package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.bw.tl.compiler.resolve.FieldContext;
import org.objectweb.asm.Type;

@EqualsAndHashCode(callSuper = true)
public @Data class ExpressionFieldAccess extends Expression {

    private final Expression precedingExpr;
    private final String fieldName;

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        final FieldContext ctx = resolver.resolveFieldCtx(precedingExpr, fieldName);

        if (ctx == null)
            return null;

        return ctx.getTypeDescriptor();
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitExpressionFieldAccess(this);
    }
}
