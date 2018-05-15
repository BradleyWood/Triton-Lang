package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ShortType extends IntType {

    public ShortType() {
        super("S");
    }

    @Override
    public boolean toShort(final MethodVisitor mv) {
        return true;
    }

    @Override
    public void arrayLoad(final MethodVisitor mv) {
        mv.visitInsn(SALOAD);
    }

    @Override
    public void arrayStore(final MethodVisitor mv) {
        mv.visitInsn(SASTORE);
    }
}
