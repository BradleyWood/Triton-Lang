package org.bw.tl.compiler.resolve;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.LinkedList;
import java.util.List;

import static org.bw.tl.util.TypeUtilities.getTypeFromName;

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

    private final boolean isCmpOp;

    public Operator(@NotNull final String name, final int opcode, @NotNull final Type lhs, @NotNull final Type rhs,
                    @NotNull final Type resultType, final boolean isCmpOp) {
        this(name, opcode, -1, lhs, rhs, resultType, isCmpOp);
    }

    public Operator(@NotNull final String name, final int opcode, final int branchOpcode, @NotNull final Type lhs, @NotNull final Type rhs,
                    @NotNull final Type resultType, final boolean isCmpOp) {
        this.name = name;
        this.opcode = opcode;
        this.branchOpcode = branchOpcode;
        this.lhs = lhs;
        this.rhs = rhs;
        this.resultType = resultType;
        this.isCmpOp = isCmpOp;
    }

    /**
     * Apply the operator. If this operator is a comparison operator
     * a 1 will be left on the stack if the result is true, otherwise 0
     *
     * @param mv the method visitor to use to write instructions
     */
    public void apply(final MethodVisitor mv) {
        if (isCmpOp) {
            final Label falseLabel = new Label();
            final Label after = new Label();
            applyCmp(mv, falseLabel);

            mv.visitInsn(ICONST_1);
            mv.visitJumpInsn(GOTO, after);

            mv.visitLabel(falseLabel);

            mv.visitInsn(ICONST_0);
            mv.visitLabel(after);
        } else {
            mv.visitInsn(opcode);
        }
    }

    /**
     * Applies the comparison and performs a jump if the comparison is false
     *
     * @param mv       the method visitor to use to write instructions
     * @param jmpLabel The label to jump to if the comparison is false
     */
    public void applyCmp(@NotNull final MethodVisitor mv, @NotNull final Label jmpLabel) {
        if (branchOpcode != -1) {
            mv.visitInsn(opcode);
            mv.visitJumpInsn(branchOpcode, jmpLabel);
        } else {
            mv.visitJumpInsn(opcode, jmpLabel);
        }
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
        addOperator("-", FSUB, "float", "long", "int", "short", "char", "byte");
        addOperator("-", DSUB, "double", "long", "float", "int", "short", "char", "byte");
        addOperator("-", ISUB, "int", "short", "char", "byte");
        addOperator("-", ISUB, "short", "char", "byte");
        addOperator("-", ISUB, "char");
        addOperator("-", ISUB, "byte");

        addOperator("*", LMUL, "long", "int", "short", "char", "byte");
        addOperator("*", FMUL, "float", "long", "int", "short", "char", "byte");
        addOperator("*", DMUL, "double", "long", "float", "int", "short", "char", "byte");
        addOperator("*", IMUL, "int", "short", "char", "byte");
        addOperator("*", IMUL, "short", "char", "byte");
        addOperator("*", IMUL, "char");
        addOperator("*", IMUL, "byte");

        addOperator("/", LDIV, "long", "int", "short", "char", "byte");
        addOperator("/", FDIV, "float", "long", "int", "short", "char", "byte");
        addOperator("/", DDIV, "double", "long", "float", "int", "short", "char", "byte");
        addOperator("/", IDIV, "int", "short", "char", "byte");
        addOperator("/", IDIV, "short", "char", "byte");
        addOperator("/", IDIV, "char");
        addOperator("/", IDIV, "byte");

        addOperator("%", LREM, "long", "int", "short", "char", "byte");
        addOperator("%", FREM, "float", "long", "int", "short", "char", "byte");
        addOperator("%", DREM, "double", "long", "float", "int", "short", "char", "byte");
        addOperator("%", IREM, "int", "short", "char", "byte");
        addOperator("%", IREM, "short", "char", "byte");
        addOperator("%", IREM, "char");
        addOperator("%", IREM, "byte");

        addCmpOperator("==", LCMP, IFNE, "long", "int", "short", "char", "byte");
        addCmpOperator("==", FCMPG, IFNE, "float", "long", "int", "short", "char", "byte");
        addCmpOperator("==", DCMPG, IFNE, "double", "long", "float", "int", "short", "char", "byte");
        addCmpOperator("==", IF_ICMPNE, "int", "short", "char", "byte");
        addCmpOperator("==", IF_ICMPNE, "short", "char", "byte");
        addCmpOperator("==", IF_ICMPNE, "char");
        addCmpOperator("==", IF_ICMPNE, "byte");

        addCmpOperator("!=", LCMP, IFEQ, "long", "int", "short", "char", "byte");
        addCmpOperator("!=", FCMPG, IFEQ, "float", "long", "int", "short", "char", "byte");
        addCmpOperator("!=", DCMPG, IFEQ, "double", "long", "float", "int", "short", "char", "byte");
        addCmpOperator("!=", IF_ICMPEQ, "int", "short", "char", "byte");
        addCmpOperator("!=", IF_ICMPEQ, "short", "char", "byte");
        addCmpOperator("!=", IF_ICMPEQ, "char");
        addCmpOperator("!=", IF_ICMPEQ, "byte");

        addCmpOperator(">", LCMP, IFLE, "long", "int", "short", "char", "byte");
        addCmpOperator(">", FCMPG, IFLE, "float", "long", "int", "short", "char", "byte");
        addCmpOperator(">", DCMPG, IFLE, "double", "long", "float", "int", "short", "char", "byte");
        addCmpOperator(">", IF_ICMPLE, "int", "short", "char", "byte");
        addCmpOperator(">", IF_ICMPLE, "short", "char", "byte");
        addCmpOperator(">", IF_ICMPLE, "char");
        addCmpOperator(">", IF_ICMPLE, "byte");

        addCmpOperator("<", LCMP, IFGE, "long", "int", "short", "char", "byte");
        addCmpOperator("<", FCMPG, IFGE, "float", "long", "int", "short", "char", "byte");
        addCmpOperator("<", DCMPG, IFGE, "double", "long", "float", "int", "short", "char", "byte");
        addCmpOperator("<", IF_ICMPGE, "int", "short", "char", "byte");
        addCmpOperator("<", IF_ICMPGE, "short", "char", "byte");
        addCmpOperator("<", IF_ICMPGE, "char");
        addCmpOperator("<", IF_ICMPGE, "byte");

        addCmpOperator(">=", LCMP, IFLT, "long", "int", "short", "char", "byte");
        addCmpOperator(">=", FCMPG, IFLT, "float", "long", "int", "short", "char", "byte");
        addCmpOperator(">=", DCMPG, IFLT, "double", "long", "float", "int", "short", "char", "byte");
        addCmpOperator(">=", IF_ICMPLT, "int", "short", "char", "byte");
        addCmpOperator(">=", IF_ICMPLT, "short", "char", "byte");
        addCmpOperator(">=", IF_ICMPLT, "char");
        addCmpOperator(">=", IF_ICMPLT, "byte");

        addCmpOperator("<=", LCMP, IFGT, "long", "int", "short", "char", "byte");
        addCmpOperator("<=", FCMPG, IFGT, "float", "long", "int", "short", "char", "byte");
        addCmpOperator("<=", DCMPG, IFGT, "double", "long", "float", "int", "short", "char", "byte");
        addCmpOperator("<=", IF_ICMPGT, "int", "short", "char", "byte");
        addCmpOperator("<=", IF_ICMPGT, "short", "char", "byte");
        addCmpOperator("<=", IF_ICMPGT, "char");
        addCmpOperator("<=", IF_ICMPGT, "byte");

        addCmpOperator("==", IF_ICMPNE, "boolean");
        addCmpOperator("!=", IF_ICMPEQ, "boolean");
    }

    public static void addOperator(@NotNull final String name, final int opcode, final @NotNull String resultType,
                                   @NotNull final String... applicableTypes) {
        final Type rt = getTypeFromName(resultType);

        operators.add(new Operator(name, opcode, rt, rt, rt, false));
        for (final String t : applicableTypes) {
            operators.add(new Operator(name, opcode, getTypeFromName(t), rt, rt, false));
        }
    }

    public static void addCmpOperator(@NotNull final String name, final int opcode, final int branchOpcode, final @NotNull String type,
                                      @NotNull final String... applicableTypes) {
        final Type pt = getTypeFromName(type);
        final Type rt = getTypeFromName("boolean");

        operators.add(new Operator(name, opcode, branchOpcode, pt, pt, rt, true));
        for (final String t : applicableTypes) {
            operators.add(new Operator(name, opcode, branchOpcode, getTypeFromName(t), pt, rt, true));
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
                if ((operator.lhs.equals(lhs) && operator.rhs.equals(rhs)) || (operator.lhs.equals(rhs) && operator.rhs.equals(lhs))) {
                    return new Operator(operator.getName(), operator.getOpcode(), operator.getBranchOpcode(), lhs, rhs, operator.getResultType(), operator.isCmpOp);
                }
            }
        }
        return null;
    }
}
