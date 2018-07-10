package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class FloatType extends Type {

    private FloatType() {
        super("F");
    }

    @Override
    public boolean toInt(final MethodVisitor mv) {
        mv.visitInsn(F2I);
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
        mv.visitInsn(F2L);
        return true;
    }

    @Override
    public boolean toFloat(final MethodVisitor mv) {
        return true;
    }

    @Override
    public boolean toDouble(final MethodVisitor mv) {
        mv.visitInsn(F2D);
        return true;
    }

    @Override
    public boolean toObject(final MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        return true;
    }

    @Override
    public void ret(final MethodVisitor mv) {
        mv.visitInsn(FRETURN);
    }

    @Override
    public boolean load(final MethodVisitor mv) {
        mv.visitInsn(FLOAD);
        return true;
    }

    @Override
    public boolean store(final MethodVisitor mv) {
        mv.visitInsn(FSTORE);
        return true;
    }

    @Override
    public boolean arrayLoad(final MethodVisitor mv) {
        mv.visitInsn(FALOAD);
        return true;
    }

    @Override
    public boolean arrayStore(final MethodVisitor mv) {
        mv.visitInsn(FASTORE);
        return true;
    }

    @Override
    public boolean cast(final MethodVisitor mv, final Type from) {
        if (from.isPrimitive() && !(from instanceof VoidType) && !(from instanceof BoolType)) {
            return from.toFloat(mv);
        }
        return false;
    }

    public static final FloatType INSTANCE = new FloatType();
}
