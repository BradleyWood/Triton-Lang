package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ShortType extends IntType {

    private ShortType() {
        super("S");
    }

    @Override
    public boolean toShort(final MethodVisitor mv) {
        return true;
    }

    @Override
    public boolean arrayLoad(final MethodVisitor mv) {
        mv.visitInsn(SALOAD);
        return true;
    }

    @Override
    public boolean arrayStore(final MethodVisitor mv) {
        mv.visitInsn(SASTORE);
        return true;
    }

    @Override
    public boolean cast(final MethodVisitor mv, final Type from) {
        if (from.isPrimitive() && !(from instanceof VoidType) && !(from instanceof BoolType)) {
            return from.toShort(mv);
        }
        return false;
    }

    public static final ShortType INSTANCE = new ShortType();
}
