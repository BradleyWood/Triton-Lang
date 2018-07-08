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

    public QualifiedName(@NotNull final String... names) {
        this.names = names;
    }

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
            return new QualifiedName();

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
    public boolean endsWith(@NotNull final String str) {
        return length() > 0 && names[names.length - 1].equals(str);
    }

    /**
     * Checks if the first name in this qualified name is equal to the specified string
     *
     * @param str The string to compare
     * @return True if the specified string is equal to the first name this fqn
     */
    public boolean beginsWith(@NotNull final String str) {
        return length() > 0 && names[0].equals(str);
    }

    /**
     * Creates a new fqn with the specified sub-indices of this fqn
     *
     * @param beginIdx the begin index
     * @param endIdx the last index
     * @return a new qualified name with the specified bounds
     */
    public QualifiedName subname(final int beginIdx, final int endIdx) {
        if (beginIdx < 0) {
            throw new StringIndexOutOfBoundsException(beginIdx);
        }
        if (endIdx > length()) {
            throw new StringIndexOutOfBoundsException(endIdx);
        }
        int subLen = endIdx - beginIdx;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }

        final String[] names = new String[endIdx - beginIdx];
        System.arraycopy(this.names, beginIdx, names, 0, subLen);

        return new QualifiedName(names);
    }

    @Override
    public void accept(@NotNull final ASTVisitor visitor) {
        visitor.visitName(this);
    }

    @Override
    public Type resolveType(@NotNull final ExpressionResolver resolver) {
        return resolver.resolveName(this);
    }

    /**
     * @return The number of names in the fqn
     */
    public int length() {
        return names.length;
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

    public String toInternalName() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            builder.append(names[i]);
            if (i + 1 < names.length) {
                builder.append("/");
            }
        }
        return builder.toString();
    }

    public String getDesc() {
        return "L" + toInternalName() + ";";
    }
}
