package org.bw.tl.compiler.types;

import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class CharHandler extends IntHandler {

    private CharHandler() {
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
    public boolean newArray(final MethodVisitor mv) {
        mv.visitIntInsn(NEWARRAY, T_CHAR);
        return true;
    }

    @Override
    public boolean cast(final MethodVisitor mv, final TypeHandler from) {
        if (from.isPrimitive() && !(from instanceof VoidHandler) && !(from instanceof BoolHandler)
                || Type.getType(from.getDesc()).equals(Type.getType(Character.class))) {
            return from.toChar(mv);
        }
        return false;
    }

    public static final CharHandler INSTANCE = new CharHandler();
}
