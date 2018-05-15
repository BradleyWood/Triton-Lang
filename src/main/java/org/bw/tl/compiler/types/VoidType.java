package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class VoidType extends Type {

    public VoidType() {
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
    public void toObject(final MethodVisitor mv) {

    }

    @Override
    public void ret(final MethodVisitor mv) {
        mv.visitInsn(RETURN);
    }

    @Override
    public void load(final MethodVisitor mv) {

    }

    @Override
    public void store(final MethodVisitor mv) {

    }

    @Override
    public void arrayLoad(final MethodVisitor mv) {

    }

    @Override
    public void arrayStore(final MethodVisitor mv) {

    }
}
