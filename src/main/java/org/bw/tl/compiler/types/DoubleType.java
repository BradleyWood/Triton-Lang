package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class DoubleType extends Type {

    public DoubleType() {
        super("D");
    }

    @Override
    public boolean toInt(final MethodVisitor mv) {
        mv.visitInsn(D2I);
        return true;
    }

    @Override
    public boolean toShort(final MethodVisitor mv) {
        toInt(mv);
        mv.visitInsn(I2S);
        return true;
    }

    @Override
    public boolean toChar(final MethodVisitor mv) {
        toInt(mv);
        mv.visitInsn(I2C);
        return true;
    }

    @Override
    public boolean toByte(final MethodVisitor mv) {
        toInt(mv);
        mv.visitInsn(I2B);
        return true;
    }

    @Override
    public boolean toLong(final MethodVisitor mv) {
        mv.visitInsn(D2L);
        return true;
    }

    @Override
    public boolean toFloat(final MethodVisitor mv) {
        mv.visitInsn(D2F);
        return true;
    }

    @Override
    public boolean toDouble(final MethodVisitor mv) {
        return true;
    }

    @Override
    public boolean toObject(final MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        return true;
    }

    @Override
    public void ret(final MethodVisitor mv) {
        mv.visitInsn(DRETURN);
    }

    @Override
    public boolean load(final MethodVisitor mv) {
        mv.visitInsn(DLOAD);
        return true;
    }

    @Override
    public boolean store(final MethodVisitor mv) {
        mv.visitInsn(DSTORE);
        return true;
    }

    @Override
    public boolean arrayLoad(final MethodVisitor mv) {
        mv.visitInsn(DALOAD);
        return true;
    }

    @Override
    public boolean arrayStore(final MethodVisitor mv) {
        mv.visitInsn(DASTORE);
        return true;
    }
}
