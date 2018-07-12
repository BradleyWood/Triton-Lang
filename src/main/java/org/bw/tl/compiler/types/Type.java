package org.bw.tl.compiler.types;

import lombok.Data;
import org.bw.tl.util.TypeUtilities;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public abstract @Data class Type {

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
        return cast(mv, new AnyType(from));
    }

    public boolean cast(final MethodVisitor mv, final Type from) {
        if (!isPrimitive() && !from.isPrimitive() && TypeUtilities.isAssignableFrom(from.getDesc(), getDesc())) {
            mv.visitTypeInsn(CHECKCAST, from.getInternalName());
            return true;
        }
        return false;
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

    public abstract boolean load(final MethodVisitor mv);

    public abstract boolean store(final MethodVisitor mv);

    public abstract boolean newArray(final MethodVisitor mv);

    public abstract boolean arrayLoad(final MethodVisitor mv);

    public abstract boolean arrayStore(final MethodVisitor mv);
}
