package org.bw.tl.compiler.types;

import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ShortHandler extends IntHandler {

    private ShortHandler() {
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
    public boolean cast(final MethodVisitor mv, final TypeHandler from) {
        if (from.isNumber()) {
            return from.toShort(mv);
        }

        return false;
    }

    @Override
    public boolean newArray(final MethodVisitor mv) {
        mv.visitIntInsn(NEWARRAY, T_SHORT);
        return true;
    }

    public static final ShortHandler INSTANCE = new ShortHandler();
}
