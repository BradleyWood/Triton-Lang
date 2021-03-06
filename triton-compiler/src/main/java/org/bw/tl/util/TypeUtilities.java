package org.bw.tl.util;

import org.bw.tl.antlr.ast.QualifiedName;
import org.bw.tl.compiler.types.AnyTypeHandler;
import org.bw.tl.compiler.types.Primitive;
import org.bw.tl.compiler.types.TypeHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;

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
            } catch (Throwable ignored) {
            }
            return false;
        } else if (f == null || t == null) {
            return false;
        }

        if (t == Primitive.INT && (f == Primitive.SHORT || f == Primitive.BYTE || f == Primitive.CHAR))
            return true;

        return t == Primitive.SHORT && (f == Primitive.BYTE);
    }

    /**
     * Counts the number of types between child and parent
     *
     * @param childType
     * @param parentType
     * @return
     */
    public static int countToParent(final Type childType, final Type parentType) {
        if (childType.equals(parentType))
            return 0;

        int count = 0;

        try {
            final Class<?> parentClass = Class.forName(parentType.getClassName());
            Class<?> cl = Class.forName(childType.getClassName());

            if (!parentClass.isAssignableFrom(cl)) {
                return -1;
            }

            while (!cl.equals(parentClass)) {
                count++;
                cl = cl.getSuperclass();

                if (cl == null) {
                    count = 0;
                    cl = Class.forName(childType.getClassName());
                    break;
                }
            }

            outer:
            for (final Class<?> iface : cl.getInterfaces()) {
                cl = iface;

                while (!cl.equals(parentClass)) {
                    count++;
                    cl = cl.getSuperclass();

                    if (cl == null) {
                        count = 0;
                        break outer;
                    }
                }

                return count;
            }

        } catch (ClassNotFoundException e) {
            return -1;
        }

        return count;
    }

    public static boolean isAssignableWithImplicitCast(final String fromDesc, final String toDesc) {
        return isAssignableWithImplicitCast(Type.getType(fromDesc), Type.getType(toDesc));
    }

    public static boolean isAssignableWithImplicitCast(final Type from, final Type to) {
        final Primitive f = Primitive.getPrimitiveByDesc(from.getDescriptor());
        final Primitive t = Primitive.getPrimitiveByDesc(to.getDescriptor());

        if (to.equals(Type.getType(Object.class)) && from != Type.VOID_TYPE)
            return true;

        if (f != null && t == null) {
            final Primitive wrappedType = Primitive.getPrimitiveFromWrapper(to.getDescriptor());

            if (wrappedType == null)
                return false;

            if (isAssignableFrom(from, Type.getType(wrappedType.getDesc())))
                return true;
        } else if (t != null && f == null) {
            final Primitive wrappedType = Primitive.getPrimitiveFromWrapper(from.getDescriptor());

            if (wrappedType == null)
                return false;

            if (isAssignableFrom(Type.getType(wrappedType.getDesc()), to))
                return true;
        }

        if (t == Primitive.DOUBLE && f != Primitive.VOID && f != Primitive.BOOL && f != Primitive.DOUBLE && f != null)
            return true;
        if (t == Primitive.FLOAT && f != Primitive.VOID && f != Primitive.BOOL && f != Primitive.DOUBLE && f != null)
            return true;

        return t == Primitive.LONG && (f == Primitive.INT || f == Primitive.SHORT || f == Primitive.BYTE || f == Primitive.CHAR);
    }

    /**
     * Returns the method signature including name and argument types
     *
     * Should only be used on a method Type
     *
     * @param type The method type
     * @return The signature
     */
    public static String getMethodSignature(final String name, final Type type) {
        final Type[] types = type.getArgumentTypes();
        final StringBuilder sb = new StringBuilder(name);

        sb.append("(");

        for (Type t : types) {
            sb.append(t.getDescriptor());
        }

        sb.append(")");
        return sb.toString();
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
            final Primitive p = Primitive.getPrimitiveByName(name.getName());
            if (p != null) {
                return Type.getType(p.getDesc());
            }
        }

        try {
            return Type.getType(Class.forName(name.getName()));
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

    public static QualifiedName getNameFromImports(@NotNull final List<QualifiedName> imports, @NotNull final QualifiedName name) {
        for (final QualifiedName imp : imports) {

            if (name.length() == 1 && imp.endsWith(name.getNames()[0]) || Arrays.equals(imp.getNames(), name.getNames())) {
                return imp;
            }
        }

        return name;
    }

    public static int getDim(@NotNull final Type type) {
        final String desc = type.getDescriptor();
        int arrayDim = 0;

        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) == '[')
                arrayDim++;
        }

        return arrayDim;
    }

    public static Type setDim(@NotNull final Type type, final int dim) {
        if (dim < 0)
            throw new IllegalArgumentException("Negative array dimension");

        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < dim; i++) {
            stringBuilder.append('[');
        }

        if (getDim(type) > 0) {
            stringBuilder.append(type.getElementType().getDescriptor());
        } else {
            stringBuilder.append(type.getDescriptor());
        }

        return Type.getType(stringBuilder.toString());
    }

    @NotNull
    public static TypeHandler getTypeHandler(@NotNull final Type type) {
        final String desc = type.getDescriptor();
        final Primitive primitive = Primitive.getPrimitiveByDesc(desc);

        if (primitive != null)
            return primitive.getTypeHandler();

        return new AnyTypeHandler(desc);
    }

    public static boolean isMethodType(final Type type) {
        return type.getDescriptor().contains("(");
    }

    public static boolean isInterface(final String type) {
        try {
            return Class.forName(type.replace("/", ".")).isInterface();
        } catch (final ClassNotFoundException e) {
        }

        return false;
    }
}
