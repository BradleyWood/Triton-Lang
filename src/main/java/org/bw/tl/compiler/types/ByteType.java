package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ByteType extends IntType {

    private ByteType() {
        super("B");
    }

    @Override
    public boolean toByte(final MethodVisitor mv) {
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
    public boolean cast(final MethodVisitor mv, final Type from) {
        if (from.isPrimitive() && !(from instanceof VoidType) && !(from instanceof BoolType)) {
            return from.toByte(mv);
        }
        return false;
    }

    public static final ByteType INSTANCE = new ByteType();
}
