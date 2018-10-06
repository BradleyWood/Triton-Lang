package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public @Data class When extends Expression {

    @Nullable
    private final Expression data;
    @NotNull
    private final List<WhenCase> cases;

    @Nullable
    private final Node elseBranch;

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        if (shouldPop())
            return null;

        return resolver.resolveWhen(this);
    }

    public boolean isExhaustive() {
        return elseBranch != null;
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitWhen(this);
    }
}
