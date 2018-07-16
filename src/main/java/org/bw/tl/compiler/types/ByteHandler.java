package org.bw.tl.compiler.types;

import jdk.internal.org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ByteHandler extends IntHandler {

    private ByteHandler() {
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
    public boolean newArray(final MethodVisitor mv) {
        mv.visitIntInsn(NEWARRAY, T_BYTE);
        return true;
    }

    @Override
    public boolean cast(final MethodVisitor mv, final TypeHandler from) {
        if (from.isPrimitive() && !(from instanceof VoidHandler) && !(from instanceof BoolHandler)
                || Type.getType(from.getDesc()).equals(Type.getType(Byte.class))) {
            return from.toByte(mv);
        }
        return false;
    }

    public static final ByteHandler INSTANCE = new ByteHandler();
}
