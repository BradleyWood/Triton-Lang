package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class BoolType extends Type {

    private BoolType() {
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
    public boolean load(final MethodVisitor mv) {
        mv.visitInsn(ILOAD);
        return true;
    }

    @Override
    public boolean store(final MethodVisitor mv) {
        mv.visitInsn(ISTORE);
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

    public static final BoolType INSTANCE = new BoolType();
}
