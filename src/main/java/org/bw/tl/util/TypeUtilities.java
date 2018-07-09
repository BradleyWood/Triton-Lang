package org.bw.tl.util;

import org.bw.tl.compiler.types.Primitive;
import org.objectweb.asm.Type;

public class TypeUtilities {

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

    public static boolean isAssignableWithImplicitCast(final Type from, final Type to) {
        final Primitive f = Primitive.getPrimitiveByDesc(from.getDescriptor());
        final Primitive t = Primitive.getPrimitiveByDesc(to.getDescriptor());

        if (t == Primitive.DOUBLE && f == Primitive.FLOAT)
            return true;

        return t == Primitive.LONG && (f == Primitive.INT || f == Primitive.SHORT || f == Primitive.BYTE || f == Primitive.CHAR);
    }
}
