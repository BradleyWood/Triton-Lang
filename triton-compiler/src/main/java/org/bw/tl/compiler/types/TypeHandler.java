package org.bw.tl.compiler.types;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.bw.tl.util.TypeUtilities.isAssignableFrom;
import static org.objectweb.asm.Opcodes.*;

@EqualsAndHashCode
public abstract @Data class TypeHandler {

    @NotNull
    private final String desc;

    public final boolean isPrimitive() {
        return desc.length() == 1;
    }

    @NotNull
    public final String getInternalName() {
        if (isPrimitive())
            return desc;
        return desc.substring(1, desc.length() - 1);
    }

    public boolean cast(final MethodVisitor mv, final String from) {
        return cast(mv, new AnyTypeHandler(from));
    }

    public boolean cast(final MethodVisitor mv, final TypeHandler from) {
        if(equals(from) || isAssignableFrom(Type.getType(from.getDesc()), Type.getType(getDesc())))
            return true;

        if (!isPrimitive() && !from.isPrimitive() && isAssignableFrom(from.getDesc(), getDesc())) {
            mv.visitTypeInsn(CHECKCAST, from.getInternalName());
            return true;
        }
        return false;
    }

    public boolean multiNewArray(final MethodVisitor mv, final int d) {
        if (Type.getType(desc).equals(Type.VOID_TYPE))
            return false;

        final StringBuilder type = new StringBuilder();

        for (int i = 0; i < d; i++) {
            type.append('[');
        }
        type.append(getDesc());

        mv.visitMultiANewArrayInsn(type.toString(), d);

        return true;
    }

    public void convertToString(final MethodVisitor mv) {
        toObject(mv);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
    }

    public abstract boolean toInt(final MethodVisitor mv);

    public abstract boolean toShort(final MethodVisitor mv);

    public abstract boolean toChar(final MethodVisitor mv);

    public abstract boolean toByte(final MethodVisitor mv);

    public abstract boolean toLong(final MethodVisitor mv);

    public abstract boolean toFloat(final MethodVisitor mv);

    public abstract boolean toDouble(final MethodVisitor mv);

    public abstract boolean toObject(final MethodVisitor mv);

    public abstract void ret(final MethodVisitor mv);

    public abstract boolean load(final MethodVisitor mv, final int idx);

    public abstract boolean store(final MethodVisitor mv, final int idx);

    public abstract boolean newArray(final MethodVisitor mv);

    public abstract boolean arrayLoad(final MethodVisitor mv);

    public abstract boolean arrayStore(final MethodVisitor mv);
}
