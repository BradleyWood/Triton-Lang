package org.bw.tl.compiler.types;

import lombok.Data;
import org.objectweb.asm.MethodVisitor;

public abstract @Data class Type {

    private final String desc;

    public abstract boolean toInt(final MethodVisitor mv);

    public abstract boolean toShort(final MethodVisitor mv);

    public abstract boolean toChar(final MethodVisitor mv);

    public abstract boolean toByte(final MethodVisitor mv);

    public abstract boolean toLong(final MethodVisitor mv);

    public abstract boolean toFloat(final MethodVisitor mv);

    public abstract boolean toDouble(final MethodVisitor mv);

    public abstract void toObject(final MethodVisitor mv);

    public abstract void ret(final MethodVisitor mv);

    public abstract void load(final MethodVisitor mv);

    public abstract void store(final MethodVisitor mv);

    public abstract void arrayLoad(final MethodVisitor mv);

    public abstract void arrayStore(final MethodVisitor mv);
}
