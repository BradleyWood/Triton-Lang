package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ByteType extends IntType {

    public ByteType() {
        super("B");
    }

    @Override
    public boolean toByte(final MethodVisitor mv) {
        return true;
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
