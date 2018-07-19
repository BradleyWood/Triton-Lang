package org.bw.tl.util;

import org.bw.tl.antlr.ast.QualifiedName;
import org.bw.tl.compiler.resolve.SymbolResolver;
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

    @Nullable
    public static String getFunctionDescriptor(@NotNull final SymbolResolver resolver, @NotNull final QualifiedName returnType,
                                               @NotNull final QualifiedName... parameterTypeNames) {
        final Type retType = resolver.resolveType(returnType);
        final Type[] parameterTypes = new Type[parameterTypeNames.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = resolver.resolveType(parameterTypeNames[i]);
            if (parameterTypes[i] == null)
                return null;
        }

        if (retType == null)
            return null;

        return Type.getMethodDescriptor(retType, parameterTypes);
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
}
