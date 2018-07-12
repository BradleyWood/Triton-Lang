package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class VoidType extends Type {

    private VoidType() {
        super("V");
    }

    @Override
    public boolean toInt(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean toShort(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean toChar(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean toByte(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean toLong(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean toFloat(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean toDouble(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean toObject(final MethodVisitor mv) {
        return false;
    }

    @Override
    public void ret(final MethodVisitor mv) {
        mv.visitInsn(RETURN);
    }

    @Override
    public boolean load(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean store(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean newArray(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean arrayLoad(final MethodVisitor mv) {
        return false;
    }

    @Override
    public boolean arrayStore(final MethodVisitor mv) {
        return false;
    }

    public static final VoidType INSTANCE = new VoidType();
}
