package org.bw.tl.compiler.types;


import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Primitive {

    INT("int", "I", "Ljava/lang/Integer;", IntHandler.INSTANCE),
    SHORT("short", "S", "Ljava/lang/Short;", ShortHandler.INSTANCE),
    CHAR("char", "C", "Ljava/lang/Character;", CharHandler.INSTANCE),
    BYTE("byte", "B", "Ljava/lang/Byte;", ByteHandler.INSTANCE),
    LONG("long", "J", "Ljava/lang/Long;", LongHandler.INSTANCE),
    FLOAT("float", "F", "Ljava/lang/Float;", FloatHandler.INSTANCE),
    DOUBLE("double", "D", "Ljava/lang/Double;", DoubleHandler.INSTANCE),
    VOID("void", "V", null, VoidHandler.INSTANCE),
    BOOL("boolean", "Z", "Ljava/lang/Boolean;", BoolHandler.INSTANCE);

    private final @Getter String name;
    private final @Getter String desc;
    private final @Getter String wrappedType;
    private final @Getter TypeHandler typeHandler;

    Primitive(final String name, final String desc, final String wrappedType, final TypeHandler typeHandler) {
        this.name = name;
        this.desc = desc;
        this.wrappedType = wrappedType;
        this.typeHandler = typeHandler;
    }

    @Nullable
    public static Primitive getPrimitiveByDesc(@Nullable final String desc) {
        if (desc == null)
            return null;

        for (final Primitive primitive : values()) {
            if (primitive.desc.equals(desc)) {
                return primitive;
            }
        }
        return null;
    }

    @Nullable
    public static Primitive getPrimitiveByName(@Nullable final String name) {
        if (name == null)
            return null;

        for (final Primitive primitive : values()) {
            if (primitive.name.equals(name)) {
                return primitive;
            }
        }
        return null;
    }

    @Nullable
    public static Primitive getPrimitiveFromWrapper(@Nullable final Class<?> clazz) {
        if (clazz == null)
            return null;

        final String internalName = "L" + clazz.getTypeName().replace(".", "/") + ";";

        for (final Primitive primitive : values()) {
            if (internalName.equals(primitive.wrappedType)) {
                return primitive;
            }
        }

        return null;
    }

    @Nullable
    public static Primitive getPrimitiveFromWrapper(@NotNull final String desc) {
        for (final Primitive primitive : values()) {
            if (desc.equals(primitive.wrappedType)) {
                return primitive;
            }
        }

        return null;
    }
}
