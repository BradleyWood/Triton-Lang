package org.bw.tl.compiler.operator;


import org.bw.tl.compiler.resolve.Operator;
import org.bw.tl.compiler.types.Primitive;
import org.bw.tl.compiler.types.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collection;
import java.util.LinkedList;

import static org.objectweb.asm.ClassWriter.*;

@RunWith(Parameterized.class)
public class TypeOpTest implements Opcodes {

    private static final String[] TYPES = {"byte", "short", "int", "long", "float", "double"};

    private final Type typeHandler;
    private final Number number;

    public TypeOpTest(final Type typeHandler, final Number number) {
        this.typeHandler = typeHandler;
        this.number = number;
    }

    @Test
    public void eval() throws Exception {
        final ClassWriter cw = new ClassWriter(COMPUTE_MAXS + COMPUTE_FRAMES);

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "org/bw/tl/compiler/CmpTest", null,
                "java/lang/Object", null);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "TEST_METHOD", "()V",
                null, null);
        mv.visitCode();
        // method body begin

        org.objectweb.asm.Type type = org.objectweb.asm.Type.getType(typeHandler.getDesc());
        Operator op = Operator.getOperator("==", type, type);

        Label throwLabel = new Label();
        Label endLabel = new Label();

        mv.visitInsn(ICONST_1);
        typeHandler.newArray(mv);
        mv.visitVarInsn(ASTORE, 0);

        // variables
        // idx 0 -> array of specified type, size 1
        // idx 1 -> val of specified type

        // stack
        // number, number?

        {
            mv.visitLdcInsn(number);
            // test array load, store, cmp
            mv.visitVarInsn(ALOAD, 0); // load array
            mv.visitInsn(ICONST_0);
            mv.visitLdcInsn(number);
            typeHandler.arrayStore(mv); // set item at 0th idx

            mv.visitVarInsn(ALOAD, 0); // load array
            mv.visitInsn(ICONST_0);
            typeHandler.arrayLoad(mv); // get item at 0th idx

            op.applyCmp(mv, throwLabel);
        }

        {
            mv.visitLdcInsn(number);
            mv.visitLdcInsn(number);
            typeHandler.store(mv, 1);
            typeHandler.load(mv, 1);

            op.applyCmp(mv, throwLabel);
        }

        {
            mv.visitLdcInsn(number);
            mv.visitMethodInsn(INVOKESTATIC, "org/bw/tl/compiler/CmpTest", "retTest",
                    "()" + type.getDescriptor(), false);
            op.applyCmp(mv, throwLabel);
        }


        mv.visitJumpInsn(GOTO, endLabel);

        mv.visitLabel(throwLabel);

        mv.visitTypeInsn(NEW, "java/lang/Exception");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false);
        mv.visitInsn(ATHROW);

        mv.visitLabel(endLabel);

        // method body end
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        {

            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "retTest", "()" + type.getDescriptor(), null, null);
            mv.visitCode();

            mv.visitLdcInsn(number);
            typeHandler.ret(mv);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        ClassLoader cl = new ClassLoader() {
            @Override
            public Class<?> findClass(String name) {
                return defineClass(name, bytes, 0, bytes.length);
            }
        };

        try {
            cl.loadClass("org.bw.tl.compiler.CmpTest").getMethod("TEST_METHOD").invoke(null);
        } catch (VerifyError e) {
            throw e;
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        final LinkedList<Object[]> parameters = new LinkedList<>();

        for (final String type : TYPES) {
            final Type typeHelper = Primitive.getPrimitiveByName(type).getPrimitiveHelper();
            parameters.add(new Object[]{typeHelper, getNumber(type)});
        }

        return parameters;
    }

    private static Number getNumber(final String type) {
        switch (type) {
            case "byte":
                return (byte) 100;
            case "short":
                return (short) 100;
            case "int":
                return 100;
            case "long":
                return 100L;
            case "float":
                return 100f;
            case "double":
                return 100.0;
            default:
                return null;
        }
    }
}
