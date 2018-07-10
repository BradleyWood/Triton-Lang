package org.bw.tl.util;

import org.bw.tl.antlr.ast.QualifiedName;
import org.bw.tl.compiler.types.Primitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

public class TypeUtilities {

    public static boolean isAssignableFrom(final String fromDesc, final String toDesc) {
        return isAssignableFrom(Type.getType(fromDesc), Type.getType(toDesc));
    }

    public static boolean isAssignableFrom(final Type from, final Type to) {
        if (from.equals(to))
            return true;

        final Primitive f = Primitive.getPrimitiveByDesc(from.getDescriptor());
        final Primitive t = Primitive.getPrimitiveByDesc(to.getDescriptor());

        if (f == null && t == null) {
            try {
                return Class.forName(to.getClassName()).isAssignableFrom(Class.forName(from.getClassName()));
            } catch (Throwable ignored){
            }
            return false;
        } else if (f == null || t == null) {
            return false;
        }

        if (t == Primitive.INT && (f == Primitive.SHORT || f == Primitive.BYTE || f == Primitive.CHAR))
            return true;

        return t == Primitive.SHORT && (f == Primitive.BYTE);
    }

    public static boolean isAssignableWithImplicitCast(final String fromDesc, final String toDesc) {
        return isAssignableWithImplicitCast(Type.getType(fromDesc), Type.getType(toDesc));
    }

    public static boolean isAssignableWithImplicitCast(final Type from, final Type to) {
        final Primitive f = Primitive.getPrimitiveByDesc(from.getDescriptor());
        final Primitive t = Primitive.getPrimitiveByDesc(to.getDescriptor());

        if (t == Primitive.DOUBLE && f == Primitive.FLOAT)
            return true;
        if (t == Primitive.FLOAT && (f == Primitive.LONG || f == Primitive.INT || f == Primitive.SHORT || f == Primitive.BYTE))
            return true;

        return t == Primitive.LONG && (f == Primitive.INT || f == Primitive.SHORT || f == Primitive.BYTE || f == Primitive.CHAR);
    }

    /**
     * Attempts to resolve a type with no contextual information. The type must
     * be primitive or fully qualified and must exists the compiler's classpath
     *
     * @param name the name to resolve
     * @return the type if it exists, otherwise null
     */
    @Nullable
    public static Type getTypeFromName(@NotNull final QualifiedName name) {
        if (name.length() == 1) {
            final Primitive p = Primitive.getPrimitiveByName(name.toString());
            if (p != null) {
                return Type.getType(p.getDesc());
            }
        }

        try {
            return Type.getType(Class.forName(name.toString()));
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    /**
     * Attempts to resolve a type with no contextual information. The type must
     * be primitive or fully qualified and must exists the compiler's classpath
     *
     * @param name the name to resolve
     * @return the type if it exists, otherwise null
     */
    public static Type getTypeFromName(final String name) {
        return getTypeFromName(QualifiedName.of(name));
    }
}
