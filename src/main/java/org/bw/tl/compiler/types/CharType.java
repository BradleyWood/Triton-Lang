package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class CharType extends IntType {

    private CharType() {
        super("C");
    }

    @Override
    public boolean toChar(final MethodVisitor mv) {
        return true;
    }

    @Override
    public boolean arrayLoad(final MethodVisitor mv) {
        mv.visitInsn(CALOAD);
        return true;
    }

    @Override
    public boolean arrayStore(final MethodVisitor mv) {
        mv.visitInsn(CASTORE);
        return true;
    }

    @Override
    public boolean cast(final MethodVisitor mv, final Type from) {
        if (from.isPrimitive() && !(from instanceof VoidType) && !(from instanceof BoolType)) {
            return from.toChar(mv);
        }
        return false;
    }

    public static final CharType INSTANCE = new CharType();
}
