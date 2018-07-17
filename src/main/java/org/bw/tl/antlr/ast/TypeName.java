package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

@EqualsAndHashCode(callSuper = true)
public @Data class TypeName extends QualifiedName {

    private int dim = 0;

    public TypeName(final int dim, final String... names) {
        super(names);
        this.dim = dim;
    }

    public TypeName(final String... names) {
        super(names);
    }

    @Override
    public void accept(@NotNull final ASTVisitor visitor) {

    }

    @Override
    public Type resolveType(@NotNull final ExpressionResolver resolver) {
        final Type componentType = super.resolveType(resolver);

        if (dim == 0 || componentType == null)
            return componentType;

        final StringBuilder descBuilder = new StringBuilder();

        for (int i = 0; i < dim; i++) {
            descBuilder.append('[');
        }

        descBuilder.append(componentType.getDescriptor());

        return Type.getType(descBuilder.toString());
    }
}
