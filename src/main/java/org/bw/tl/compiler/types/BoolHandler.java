package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class BoolHandler extends TypeHandler {

    private BoolHandler() {
        super("Z");
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
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
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
        mv.visitInsn(BALOAD);
        return true;
    }

    @Override
    public boolean arrayStore(final MethodVisitor mv) {
        mv.visitInsn(BASTORE);
        return true;
    }

    @Override
    public boolean newArray(final MethodVisitor mv) {
        mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
        return true;
    }

    @Override
    public boolean cast(final MethodVisitor mv, final TypeHandler from) {
        if (from.getDesc().equals("Ljava/lang/Boolean;")) {
            mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(), "booleanValue", "()Z", false);
            return true;
        }

        return false;
    }

    public static final BoolHandler INSTANCE = new BoolHandler();
}
