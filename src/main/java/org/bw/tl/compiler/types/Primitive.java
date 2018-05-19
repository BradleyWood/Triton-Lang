package org.bw.tl.compiler.types;


import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public enum Primitive {

    INT("int", "I", "Ljava/lang/Integer;"),
    SHORT("short", "S", "Ljava/lang/Short;"),
    CHAR("char", "C", "Ljava/lang/Character;"),
    BYTE("byte", "B", "Ljava/lang/Byte;"),
    LONG("long", "J", "Ljava/lang/Long;"),
    FLOAT("float", "F", "Ljava/lang/Float;"),
    DOUBLE("double", "D", "Ljava/lang/Double;"),
    VOID("void", "V", null),
    BOOL("boolean", "Z", "Ljava/lang/Boolean;");

    private final @Getter String name;
    private final @Getter String desc;
    private final @Getter String wrappedType;

    Primitive(final String name, final String desc, final String wrappedType) {
        this.name = name;
        this.desc = desc;
        this.wrappedType = wrappedType;
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
