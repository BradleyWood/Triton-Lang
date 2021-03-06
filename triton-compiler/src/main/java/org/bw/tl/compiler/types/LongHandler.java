package org.bw.tl.compiler.types;

import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class LongHandler extends TypeHandler {

    private LongHandler() {
        super("J");
    }

    @Override
    public boolean toInt(final MethodVisitor mv) {
        mv.visitInsn(L2I);
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
        return true;
    }

    @Override
    public boolean toFloat(final MethodVisitor mv) {
        mv.visitInsn(L2F);
        return true;
    }

    @Override
    public boolean toDouble(final MethodVisitor mv) {
        mv.visitInsn(L2D);
        return true;
    }

    @Override
    public boolean toObject(final MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(I)Ljava/lang/Long;", false);
        return true;
    }

    @Override
    public void ret(final MethodVisitor mv) {
        mv.visitInsn(LRETURN);
    }

    @Override
    public boolean load(final MethodVisitor mv, final int idx) {
        mv.visitVarInsn(LLOAD, idx);
        return true;
    }

    @Override
    public boolean store(final MethodVisitor mv, final int idx) {
        mv.visitVarInsn(LSTORE, idx);
        return true;
    }

    @Override
    public boolean arrayLoad(final MethodVisitor mv) {
        mv.visitInsn(LALOAD);
        return true;
    }

    @Override
    public boolean arrayStore(final MethodVisitor mv) {
        mv.visitInsn(LASTORE);
        return true;
    }

    @Override
    public boolean newArray(final MethodVisitor mv) {
        mv.visitIntInsn(NEWARRAY, T_LONG);
        return true;
    }

    @Override
    public boolean cast(final MethodVisitor mv, final TypeHandler from) {
        if (from.isNumber()) {
            return from.toLong(mv);
        }
        return false;
    }

    public static final LongHandler INSTANCE = new LongHandler();
}
