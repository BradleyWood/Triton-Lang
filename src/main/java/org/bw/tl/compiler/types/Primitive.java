package org.bw.tl.compiler.types;


import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public enum Primitive {

    INT("int", "I", "Ljava/lang/Integer;", IntType.INSTANCE),
    SHORT("short", "S", "Ljava/lang/Short;", ShortType.INSTANCE),
    CHAR("char", "C", "Ljava/lang/Character;", CharType.INSTANCE),
    BYTE("byte", "B", "Ljava/lang/Byte;", ByteType.INSTANCE),
    LONG("long", "J", "Ljava/lang/Long;", LongType.INSTANCE),
    FLOAT("float", "F", "Ljava/lang/Float;", FloatType.INSTANCE),
    DOUBLE("double", "D", "Ljava/lang/Double;", DoubleType.INSTANCE),
    VOID("void", "V", null, VoidType.INSTANCE),
    BOOL("boolean", "Z", "Ljava/lang/Boolean;", BoolType.INSTANCE);

    private final @Getter String name;
    private final @Getter String desc;
    private final @Getter String wrappedType;
    private final @Getter Type primitiveHelper;

    Primitive(final String name, final String desc, final String wrappedType, final Type primitiveHelper) {
        this.name = name;
        this.desc = desc;
        this.wrappedType = wrappedType;
        this.primitiveHelper = primitiveHelper;
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
}
