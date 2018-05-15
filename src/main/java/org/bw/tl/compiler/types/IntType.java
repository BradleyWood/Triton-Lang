package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class IntType extends Type {

    public IntType() {
        super("I");
    }

    @Override
    public boolean toInt(final MethodVisitor mv) {
        return true;
    }

    @Override
    public boolean toShort(final MethodVisitor mv) {
        mv.visitInsn(I2S);
        return true;
    }

    @Override
    public boolean toChar(final MethodVisitor mv) {
        mv.visitInsn(I2C);
        return true;
    }

    @Override
    public boolean toByte(final MethodVisitor mv) {
        mv.visitInsn(I2B);
        return true;
    }

    @Override
    public boolean toLong(final MethodVisitor mv) {
        mv.visitInsn(I2L);
        return true;
    }

    @Override
    public boolean toFloat(final MethodVisitor mv) {
        mv.visitInsn(I2F);
        return true;
    }

    @Override
    public boolean toDouble(final MethodVisitor mv) {
        mv.visitInsn(I2D);
        return true;
    }

    @Override
    public void toObject(final MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
    }

    @Override
    public void ret(final MethodVisitor mv) {
        mv.visitInsn(IRETURN);
    }

    @Override
    public void load(final MethodVisitor mv) {
        mv.visitInsn(ILOAD);
    }

    @Override
    public void store(final MethodVisitor mv) {
        mv.visitInsn(ISTORE);
    }

    @Override
    public void arrayLoad(final MethodVisitor mv) {
        mv.visitInsn(IALOAD);
    }

    @Override
    public void arrayStore(final MethodVisitor mv) {
        mv.visitInsn(IASTORE);
    }
}
