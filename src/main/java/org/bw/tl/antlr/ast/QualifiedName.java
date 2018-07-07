package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = false)
public @Data class QualifiedName extends Expression {

    @NotNull
    private final String[] names;

    @NotNull
    public QualifiedName append(@NotNull final String identifier) {
        final String[] names = new String[this.names.length + 1];
        System.arraycopy(this.names, 0, names, 0, this.names.length);
        names[this.names.length] = identifier;
        return new QualifiedName(names);
    }

    /**
     * Remove the last name from this qualified name
     * Example a.b.c.d ---> a.b.c
     *
     * @return
     */
    @NotNull
    public QualifiedName removeLast() {
        if (names.length < 1)
            return new QualifiedName(new String[0]);

        final String[] names = new String[this.names.length - 1];
        System.arraycopy(this.names, 0, names, 0, this.names.length - 1);
        return new QualifiedName(names);
    }

    /**
     * Checks if the last name in this fqn equals the specified string
     *
     * @param str The string to compare
     * @return
     */
    public boolean endsWith(final String str) {
        return names.length > 0 && names[names.length - 1].equals(str);
    }

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitName(this);
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return resolver.resolveName(this);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            builder.append(names[i]);
            if (i + 1 < names.length) {
                builder.append(".");
            }
        }
        return builder.toString();
    }
}
