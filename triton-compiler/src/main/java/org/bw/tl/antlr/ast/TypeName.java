package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public @Data class TypeName extends QualifiedName {

    private final LinkedList<TypeName> typeParameters = new LinkedList<>();
    private int dim = 0;

    public TypeName(final int dim, final String... names) {
        super(names);
        this.dim = dim;
    }

    public TypeName(final String... names) {
        super(names);
    }

    public void addTypeParameter(final TypeName name) {
        typeParameters.add(name);
    }

    @Override
    public void accept(@NotNull final ASTVisitor visitor) {

    }

    @Override
    public Type resolveType(@NotNull final ExpressionResolver resolver) {
        return resolver.resolveTypeName(this);
    }

    public static TypeName of(@NotNull final String name) {
        int dim = 0;
        for (char c : name.toCharArray()) {
            if (c == '[') {
                dim++;
            }
        }
        int idx = name.indexOf('[') >= 0 ? name.indexOf('[') : name.length();

        return of(name.substring(0, idx), dim);
    }

    public static TypeName of(@NotNull final String name, final int dim) {
        final TypeName typeName;
        if (name.contains(".")) {
            typeName = new TypeName(dim, name.split("\\."));
        } else {
            typeName = new TypeName(dim, name);
        }
        return typeName;
    }
}
