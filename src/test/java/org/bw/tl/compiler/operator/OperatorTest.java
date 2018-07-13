package org.bw.tl.compiler.operator;

import org.bw.tl.compiler.resolve.Operator;
import org.bw.tl.compiler.types.Primitive;
import org.bw.tl.compiler.types.TypeHandler;
import org.bw.tl.util.TypeUtilities;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import static org.bw.tl.util.TypeUtilities.isAssignableWithImplicitCast;

@RunWith(Parameterized.class)
public class OperatorTest implements Opcodes {

    private static final String[] TYPES = {"byte", "short", "int", "long", "float", "double"};
    private static final String[] OPERATORS = {"+", "-", "*", "/", "%"};
    private static final Random random = new Random();

    private final String opName;
    private final Number lhs;
    private final Number rhs;
    private final Number expected;

    public OperatorTest(@NotNull final String opName, @NotNull final Number lhs, @NotNull final Number rhs,
                        @NotNull final Number expected) {
        this.opName = opName;
        this.lhs = lhs;
        this.rhs = rhs;
        this.expected = expected;
    }

    @Test
    public void eval() throws Exception {
        final Type leftType = Type.getType(Primitive.getPrimitiveFromWrapper(lhs.getClass()).getDesc());
        final Type rightType = Type.getType(Primitive.getPrimitiveFromWrapper(rhs.getClass()).getDesc());

        final Operator op = Operator.getOperator(opName, leftType, rightType);

        if (op == null) {
            throw new Exception("Operator not found: " + leftType.getDescriptor() + " " + opName + " " + rightType.getDescriptor());
        }

        final ClassWriter cw = new ClassWriter(0);

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "org/bw/tl/compiler/OpTest", null, "java/lang/Object", null);

        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "TEST_CLASS", "()V", null, null);
        mv.visitCode();

        boolean toLong = !op.getResultType().equals(Type.FLOAT_TYPE) && !op.getResultType().equals(Type.DOUBLE_TYPE);

        if (toLong) {
            mv.visitLdcInsn(expected.longValue());
        } else {
            mv.visitLdcInsn(expected.doubleValue());
        }

        mv.visitLdcInsn(lhs);

        if (!op.getLhs().equals(op.getResultType()) && isAssignableWithImplicitCast(op.getLhs(), op.getRhs())) {
            final Primitive to = Primitive.getPrimitiveByDesc(op.getRhs().getDescriptor());
            final Primitive from = Primitive.getPrimitiveByDesc(op.getLhs().getDescriptor());
            to.getPrimitiveHelper().cast(mv, from.getPrimitiveHelper());
            // lhs must be cast
        }

        mv.visitLdcInsn(rhs);

        if (!op.getRhs().equals(op.getResultType()) && isAssignableWithImplicitCast(op.getRhs(), op.getLhs())) {
            final Primitive to = Primitive.getPrimitiveByDesc(op.getLhs().getDescriptor());
            final Primitive from = Primitive.getPrimitiveByDesc(op.getRhs().getDescriptor());
            to.getPrimitiveHelper().cast(mv, from.getPrimitiveHelper());
            // rhs must be cast
        }

        op.apply(mv);

        boolean convertToLong = TypeUtilities.isAssignableWithImplicitCast(op.getResultType(), Type.LONG_TYPE);
        boolean convertToDouble = op.getResultType().equals(Type.FLOAT_TYPE);

        TypeHandler t = Primitive.getPrimitiveByDesc(op.getResultType().getDescriptor()).getPrimitiveHelper();
        if (convertToLong) {
            t.toLong(mv);
        } else if (convertToDouble) {
            t.toDouble(mv);
        }

        if (toLong) {
            mv.visitMethodInsn(INVOKESTATIC, "org/junit/Assert", "assertEquals", "(JJ)V", false);
        } else {
            // put delta value
            mv.visitLdcInsn(0.01D);
            mv.visitMethodInsn(INVOKESTATIC, "org/junit/Assert", "assertEquals", "(DDD)V", false);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(6, 1);
        mv.visitEnd();

        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        ClassLoader cl = new ClassLoader() {
            @Override
            public Class<?> findClass(String name) {
                return defineClass(name, bytes, 0, bytes.length);
            }
        };

        try {
            cl.loadClass("org.bw.tl.compiler.OpTest").getMethod("TEST_CLASS").invoke(null);
        } catch (VerifyError e) {
            System.err.println(leftType.getClassName() + " " + opName + " " + rightType.getClassName());
            throw e;
        }
    }

    @Parameterized.Parameters(name = "Test {1} {0} {2} = {3}")
    public static Collection<Object[]> parameters() {
        final LinkedList<Object[]> parameters = new LinkedList<>();
        final boolean[] TF_MAP = {true, false};

        for (final String operator : OPERATORS) {
            for (String lhsType : TYPES) {
                for (String rhsType : TYPES) {
                    for (boolean lhsSign : TF_MAP) {
                        for (boolean rhsSign : TF_MAP) {
                            final boolean requireNonZero = operator.equals("/") || operator.equals("%");
                            final Number lhs = getNumber(lhsType, lhsSign);
                            final Number rhs = getNumber(rhsType, rhsSign, requireNonZero);
                            final Number expected = getExpected(operator, lhs, rhs);
                            parameters.add(new Object[]{operator, lhs, rhs, expected});
                        }
                    }
                }
            }
        }

        return parameters;
    }

    private static Number getNumber(final String type, boolean pos) {
        return getNumber(type, pos, false);
    }

    private static Number getNumber(final String type, boolean pos, boolean nonZero) {
        final int off = pos ? (nonZero ? 1 : 0) : (nonZero ? -1 : 0);
        switch (type) {
            case "byte":
                return (byte) (random.nextInt(Byte.MAX_VALUE) * (pos ? 1 : -1) + off);
            case "short":
                return (short) (random.nextInt(Byte.MAX_VALUE) * (pos ? 1 : -1) + off);
            case "int":
                return random.nextInt(Byte.MAX_VALUE) * (pos ? 1 : -1) + off;
            case "long":
                return (long) (random.nextInt(Byte.MAX_VALUE) * (pos ? 1 : -1) + off);
            case "float":
                return random.nextFloat() * (pos ? 100 : -100);
            case "double":
                return random.nextDouble() * (pos ? 100 : -100);
            default:
                return null;
        }
    }

    private static Number getExpected(final String opName, final Number lhs, final Number rhs) {
        if (lhs instanceof Float || lhs instanceof Double || rhs instanceof Float || rhs instanceof Double) {
            return evalDoubleOp(opName, lhs.doubleValue(), rhs.doubleValue());
        } else {
            return evalIntOp(opName, lhs.longValue(), rhs.longValue());
        }
    }

    private static Long evalIntOp(final String op, final Long lhs, final Long rhs) {
        switch (op) {
            case "+":
                return lhs + rhs;
            case "-":
                return lhs - rhs;
            case "*":
                return lhs * rhs;
            case "/":
                return lhs / rhs;
            case "%":
                return lhs % rhs;
        }
        return null;
    }

    private static Double evalDoubleOp(final String op, final Double lhs, final Double rhs) {
        switch (op) {
            case "+":
                return lhs + rhs;
            case "-":
                return lhs - rhs;
            case "*":
                return lhs * rhs;
            case "/":
                return lhs / rhs;
            case "%":
                return lhs % rhs;
        }
        return null;
    }
}
