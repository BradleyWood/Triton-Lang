package org.bw.tl.compiler.types;

import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class IntHandler extends TypeHandler {

    private IntHandler() {
        this("I");
    }

    IntHandler(final String desc) {
        super(desc);
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
    public boolean toObject(final MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        return true;
    }

    @Override
    public void ret(final MethodVisitor mv) {
        mv.visitInsn(IRETURN);
    }

    @Override
    public boolean load(final MethodVisitor mv, final int idx) {
        mv.visitVarInsn(ILOAD, idx);
        return true;
    }

    @Override
    public boolean store(final MethodVisitor mv, final int idx) {
        mv.visitVarInsn(ISTORE, idx);
        return true;
    }

    @Override
    public boolean arrayLoad(final MethodVisitor mv) {
        mv.visitInsn(IALOAD);
        return true;
    }

    @Override
    public boolean arrayStore(final MethodVisitor mv) {
        mv.visitInsn(IASTORE);
        return true;
    }

    @Override
    public boolean newArray(final MethodVisitor mv) {
        mv.visitIntInsn(NEWARRAY, T_INT);
        return true;
    }

    @Override
    public boolean cast(final MethodVisitor mv, final TypeHandler from) {
        if (from.isPrimitive() && !(from instanceof VoidHandler) && !(from instanceof BoolHandler)
                || Type.getType(from.getDesc()).equals(Type.getType(Integer.class))) {
            return from.toInt(mv);
        }
        return false;
    }

    public static final IntHandler INSTANCE = new IntHandler();
}
