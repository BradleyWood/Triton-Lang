package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class BoolType extends Type {

    public BoolType() {
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
    public void toObject(final MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
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
        mv.visitInsn(BALOAD);
    }

    @Override
    public void arrayStore(final MethodVisitor mv) {
        mv.visitInsn(BASTORE);
    }
}
