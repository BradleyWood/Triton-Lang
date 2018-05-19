package org.bw.tl.compiler.types;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;

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
        return desc.substring(1, desc.length() - 2);
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

    public abstract boolean arrayLoad(final MethodVisitor mv);

    public abstract boolean arrayStore(final MethodVisitor mv);
}
