package org.bw.tl.compiler.types;

import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class AnyType extends Type {

    private static final List<String> NUMBER_TYPES = Arrays.asList(
            "Ljava/lang/Number;",
            "Ljava/lang/Integer;",
            "Ljava/lang/Long;",
            "Ljava/lang/Short;",
            "Ljava/lang/Byte;",
            "Ljava/lang/Float;",
            "Ljava/lang/Double;"
    );

    public AnyType(final String desc) {
        super(desc);
    }

    private boolean isNumber() {
        return NUMBER_TYPES.contains(getDesc());
    }

    @Override
    public boolean toInt(final MethodVisitor mv) {
        if (isNumber()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(), "intValue", "()I", false);
            return true;
        }
        return false;
    }

    @Override
    public boolean toShort(final MethodVisitor mv) {
        if (isNumber()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(), "shortValue", "()S", false);
            return true;
        }
        return false;
    }

    @Override
    public boolean toChar(final MethodVisitor mv) {
        if (isNumber()) {
            toInt(mv);
            mv.visitInsn(I2C);
            return true;
        }
        return false;
    }

    @Override
    public boolean toByte(final MethodVisitor mv) {
        if (isNumber()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(), "byteValue", "()B", false);
            return true;
        }
        return false;
    }

    @Override
    public boolean toLong(final MethodVisitor mv) {
        if (isNumber()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(), "longValue", "()J", false);
            return true;
        }
        return false;
    }

    @Override
    public boolean toFloat(final MethodVisitor mv) {
        if (isNumber()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(), "floatValue", "()F", false);
            return true;
        }
        return false;
    }

    @Override
    public boolean toDouble(final MethodVisitor mv) {
        if (isNumber()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, getInternalName(), "doubleValue", "()D", false);
            return true;
        }
        return false;
    }

    @Override
    public boolean toObject(final MethodVisitor mv) {
        if (isPrimitive()) {
            final Primitive p = Primitive.getPrimitiveByDesc(getDesc());
            if (p != null) {
                return p.getPrimitiveHelper().toObject(mv);
            }
            return false;
        }
        return true;
    }

    @Override
    public void ret(final MethodVisitor mv) {
        mv.visitInsn(ARETURN);
    }

    @Override
    public boolean load(final MethodVisitor mv) {
        mv.visitInsn(ALOAD);
        return true;
    }

    @Override
    public boolean store(final MethodVisitor mv) {
        mv.visitInsn(ASTORE);
        return true;
    }

    @Override
    public boolean arrayLoad(final MethodVisitor mv) {
        mv.visitInsn(AALOAD);
        return true;
    }

    @Override
    public boolean arrayStore(final MethodVisitor mv) {
        mv.visitInsn(AASTORE);
        return true;
    }
}
