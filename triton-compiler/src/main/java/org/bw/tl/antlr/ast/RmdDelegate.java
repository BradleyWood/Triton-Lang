package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;

@EqualsAndHashCode(callSuper = true)
public @Data class RmdDelegate extends Expression {

    private final Block block;
    private final Expression condition;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitRmdDelegate(this);
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return resolver.resolveBlock(block);
    }
}
