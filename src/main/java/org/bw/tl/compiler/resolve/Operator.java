package org.bw.tl.compiler.resolve;

import lombok.Data;
import org.bw.tl.compiler.types.Primitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.LinkedList;
import java.util.List;

import static org.bw.tl.util.TypeUtilities.getTypeFromName;
import static org.bw.tl.util.TypeUtilities.isAssignableFrom;
import static org.bw.tl.util.TypeUtilities.isAssignableWithImplicitCast;

public @Data class Operator implements Opcodes {

    private static final List<Operator> operators = new LinkedList<>();

    @NotNull
    private final String name;
    @NotNull
    private final Type lhs;
    @NotNull
    private final Type rhs;
    @NotNull
    private final Type resultType;

    private final int opcode;

    private final int branchOpcode;

    public Operator(@NotNull final String name, final int opcode, @NotNull final Type lhs, @NotNull final Type rhs,
                    @NotNull final Type resultType) {
        this(name, opcode, -1, lhs, rhs, resultType);
    }

    public Operator(@NotNull final String name, final int opcode, final int branchOpcode, @NotNull final Type lhs, @NotNull final Type rhs,
                    @NotNull final Type resultType) {
        this.name = name;
        this.opcode = opcode;
        this.branchOpcode = branchOpcode;
        this.lhs = lhs;
        this.rhs = rhs;
        this.resultType = resultType;
    }

    /**
     * Apply the operator
     *
     * @param mv the method visitor to use to write instructions
     * @return true if the comparison is possible
     */
    public boolean apply(final MethodVisitor mv) {
        if (branchOpcode != -1 || castArgs(mv))
            return false;

        mv.visitInsn(opcode);

        return true;
    }

    /**
     * Applies the comparison and performs a jump if the comparison is false
     *
     * @param mv       the method visitor to use to write instructions
     * @param jmpLabel The label to jump to if the comparison is false
     * @return true if the comparison is possible
     */
    public boolean applyCmp(@NotNull final MethodVisitor mv, @NotNull final Label jmpLabel) {
        if (castArgs(mv))
            return false;

        if (branchOpcode != -1) {
            mv.visitInsn(opcode);
            mv.visitJumpInsn(branchOpcode, jmpLabel);
        } else {
            mv.visitJumpInsn(opcode, jmpLabel);
        }

        return true;
    }

    private boolean castArgs(MethodVisitor mv) {
        if (!lhs.equals(rhs) && !isAssignableFrom(lhs, rhs) && !isAssignableFrom(rhs, lhs)) {
            if (isAssignableWithImplicitCast(lhs, rhs)) {
                final Primitive from = Primitive.getPrimitiveByDesc(lhs.getDescriptor());
                final Primitive to = Primitive.getPrimitiveByDesc(rhs.getDescriptor());
                return to.getPrimitiveHelper().cast(mv, from.getPrimitiveHelper());
            } else if (isAssignableWithImplicitCast(rhs, lhs)) {
                final Primitive to = Primitive.getPrimitiveByDesc(lhs.getDescriptor());
                final Primitive from = Primitive.getPrimitiveByDesc(rhs.getDescriptor());
                return to.getPrimitiveHelper().cast(mv, from.getPrimitiveHelper());
            } else {
                return false;
            }
        }
        return true;
    }

    static {
        addOperator("+", LADD, "long", "int", "short", "char", "byte");
        addOperator("+", FADD, "float", "long", "int", "short", "char", "byte");
        addOperator("+", DADD, "double", "long", "float", "int", "short", "char", "byte");
        addOperator("+", IADD, "long", "int", "short", "char", "byte");
        addOperator("+", IADD, "int", "short", "char", "byte");
        addOperator("+", IADD, "short", "char", "byte");
        addOperator("+", IADD, "char");
        addOperator("+", IADD, "byte");


        addOperator("-", LSUB, "long", "int", "short", "char", "byte");
        addOperator("-", FSUB, "float", "int", "short", "char", "byte");
        addOperator("-", DSUB, "double", "float", "int", "short", "char", "byte");
        addOperator("-", ISUB, "int", "short", "char", "byte");
        addOperator("-", ISUB, "short", "char", "byte");
        addOperator("-", ISUB, "char");
        addOperator("-", ISUB, "byte");

        addOperator("*", LMUL, "long", "int", "short", "char", "byte");
        addOperator("*", FMUL, "float", "int", "short", "char", "byte");
        addOperator("*", DMUL, "double", "float", "int", "short", "char", "byte");
        addOperator("*", IMUL, "int", "short", "char", "byte");
        addOperator("*", IMUL, "short", "char", "byte");
        addOperator("*", IMUL, "char");
        addOperator("*", IMUL, "byte");

        addOperator("/", LDIV, "long", "int", "short", "char", "byte");
        addOperator("/", FDIV, "float", "int", "short", "char", "byte");
        addOperator("/", DDIV, "double", "float", "int", "short", "char", "byte");
        addOperator("/", IDIV, "int", "short", "char", "byte");
        addOperator("/", IDIV, "short", "char", "byte");
        addOperator("/", IDIV, "char");
        addOperator("/", IDIV, "byte");

        addOperator("%", LREM, "long", "int", "short", "char", "byte");
        addOperator("%", FREM, "float", "int", "short", "char", "byte");
        addOperator("%", DREM, "double", "float", "int", "short", "char", "byte");
        addOperator("%", IREM, "int", "short", "char", "byte");
        addOperator("%", IREM, "short", "char", "byte");
        addOperator("%", IREM, "char");
        addOperator("%", IREM, "byte");

        addCmpOperator("==", LCMP, IFNE, "long", "int", "short", "char", "byte");
        addCmpOperator("==", FCMPG, IFNE, "float", "int", "short", "char", "byte");
        addCmpOperator("==", DCMPG, IFNE, "double", "float", "int", "short", "char", "byte");
        addCmpOperator("==", IF_ICMPNE, "int", "short", "char", "byte");
        addCmpOperator("==", IF_ICMPNE, "short", "char", "byte");
        addCmpOperator("==", IF_ICMPNE, "char");
        addCmpOperator("==", IF_ICMPNE, "byte");

        addCmpOperator("!=", LCMP, IFEQ, "long", "int", "short", "char", "byte");
        addCmpOperator("!=", FCMPG, IFEQ, "float", "int", "short", "char", "byte");
        addCmpOperator("!=", DCMPG, IFEQ, "double", "float", "int", "short", "char", "byte");
        addCmpOperator("!=", IF_ICMPEQ, "int", "short", "char", "byte");
        addCmpOperator("!=", IF_ICMPEQ, "short", "char", "byte");
        addCmpOperator("!=", IF_ICMPEQ, "char");
        addCmpOperator("!=", IF_ICMPEQ, "byte");

        addCmpOperator(">", LCMP, IFLE, "long", "int", "short", "char", "byte");
        addCmpOperator(">", FCMPG, IFLE, "float", "int", "short", "char", "byte");
        addCmpOperator(">", DCMPG, IFLE, "double", "float", "int", "short", "char", "byte");
        addCmpOperator(">", IF_ICMPLE, "int", "short", "char", "byte");
        addCmpOperator(">", IF_ICMPLE, "short", "char", "byte");
        addCmpOperator(">", IF_ICMPLE, "char");
        addCmpOperator(">", IF_ICMPLE, "byte");

        addCmpOperator("<", LCMP, IFGE, "long", "int", "short", "char", "byte");
        addCmpOperator("<", FCMPG, IFGE, "float", "int", "short", "char", "byte");
        addCmpOperator("<", DCMPG, IFGE, "double", "float", "int", "short", "char", "byte");
        addCmpOperator("<", IF_ICMPGE, "int", "short", "char", "byte");
        addCmpOperator("<", IF_ICMPGE, "short", "char", "byte");
        addCmpOperator("<", IF_ICMPGE, "char");
        addCmpOperator("<", IF_ICMPGE, "byte");

        addCmpOperator(">=", LCMP, IFLT, "long", "int", "short", "char", "byte");
        addCmpOperator(">=", FCMPG, IFLT, "float", "int", "short", "char", "byte");
        addCmpOperator(">=", DCMPG, IFLT, "double", "float", "int", "short", "char", "byte");
        addCmpOperator(">=", IF_ICMPLT, "int", "short", "char", "byte");
        addCmpOperator(">=", IF_ICMPLT, "short", "char", "byte");
        addCmpOperator(">=", IF_ICMPLT, "char");
        addCmpOperator(">=", IF_ICMPLT, "byte");

        addCmpOperator(">=", LCMP, IFGT, "long", "int", "short", "char", "byte");
        addCmpOperator(">=", FCMPG, IFGT, "float", "int", "short", "char", "byte");
        addCmpOperator(">=", DCMPG, IFGT, "double", "float", "int", "short", "char", "byte");
        addCmpOperator(">=", IF_ICMPGT, "int", "short", "char", "byte");
        addCmpOperator(">=", IF_ICMPGT, "short", "char", "byte");
        addCmpOperator(">=", IF_ICMPGT, "char");
        addCmpOperator(">=", IF_ICMPGT, "byte");
    }

    public static void addOperator(@NotNull final String name, final int opcode, final @NotNull String resultType,
                                   @NotNull final String... applicableTypes) {
        final Type rt = getTypeFromName(resultType);

        operators.add(new Operator(name, opcode, rt, rt, rt));
        for (final String t : applicableTypes) {
            operators.add(new Operator(name, opcode, getTypeFromName(t), rt, rt));
        }
    }

    public static void addCmpOperator(@NotNull final String name, final int opcode, final int branchOpcode, final @NotNull String type,
                                      @NotNull final String... applicableTypes) {
        final Type pt = getTypeFromName(type);
        final Type rt = getTypeFromName("boolean");

        operators.add(new Operator(name, opcode, pt, pt, rt));
        for (final String t : applicableTypes) {
            operators.add(new Operator(name, opcode, branchOpcode, getTypeFromName(t), pt, rt));
        }
    }

    public static void addCmpOperator(@NotNull final String name, final int opcode, final @NotNull String type,
                                      @NotNull final String... applicableTypes) {
        addCmpOperator(name, opcode, -1, type, applicableTypes);
    }

    @Nullable
    public static Operator getOperator(@NotNull final String name, @NotNull final Type lhs, @NotNull final Type rhs) {
        for (final Operator operator : operators) {
            if (operator.getName().equals(name)) {
                if ((operator.lhs.equals(lhs) && operator.rhs.equals(rhs)) || (operator.lhs.equals(rhs) && operator.rhs.equals(lhs)))
                    return operator;
            }
        }
        return null;
    }
}
