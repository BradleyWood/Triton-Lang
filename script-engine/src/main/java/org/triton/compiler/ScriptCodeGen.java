package org.triton.compiler;

import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.MethodCtx;
import org.bw.tl.compiler.MethodImpl;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.script.Bindings;

public class ScriptCodeGen implements ASTVisitor, Opcodes {

    private final MethodImpl codeGen;
    private final MethodVisitor mv;
    private final MethodCtx ctx;

    public ScriptCodeGen(@NotNull final MethodImpl codeGen) {
        this.codeGen = codeGen;
        this.mv = codeGen.getMv();
        this.ctx = codeGen.getCtx();
    }

    @Override
    public void visitFunction(final Function function) {
        if (isEvalMethod()) {
            ctx.getScope().beginScope();

            ctx.getScope().putVar(" __this__ ", Type.getType(Object.class), 0);
            ctx.getScope().putVar(" __bindings__ ", Type.getType(Bindings.class), ACC_FINAL);
            function.getBody().accept(this);

            ctx.getScope().endScope();

            codeGen.getMv().visitInsn(RETURN);
        } else {
            codeGen.visitFunction(function);
        }
    }

    private void store(final String name, final Expression expression) {
        final String putDesc = "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;";
        final int bindingsIdx = ctx.getScope().findVar(" __bindings__ ").getIndex();

        mv.visitVarInsn(ALOAD, bindingsIdx);
        mv.visitLdcInsn(name);
        expression.accept(this);

        mv.visitMethodInsn(INVOKEINTERFACE, "javax/script/Bindings", "put", putDesc, true);
    }

    private void load(final String attribute) {
        final String containsKey = "(Ljava/lang/String;)Z";
        final String getDesc = "(Ljava/lang/String;)Ljava/lang/Object;";
        final int bindingsIdx = ctx.getScope().findVar(" __bindings__ ").getIndex();

        final Label exceptionEnd = new Label();

        mv.visitVarInsn(ALOAD, bindingsIdx);
        mv.visitLdcInsn(attribute);

        mv.visitMethodInsn(INVOKEINTERFACE, "javax/script/Bindings", "containsKey", containsKey, true);
        mv.visitJumpInsn(IF_ICMPNE, exceptionEnd);

        throwException("java/lang/NoSuchFieldException", attribute);
        mv.visitLabel(exceptionEnd);

        mv.visitVarInsn(ALOAD, bindingsIdx);
        mv.visitLdcInsn(attribute);
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/script/Bindings", "get", getDesc, true);
    }

    private void throwException(final String type, final String message) {
        mv.visitTypeInsn(NEW, type);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(String.valueOf(message));
        mv.visitMethodInsn(INVOKESPECIAL, type, "<init>", "()V", false);

        mv.visitInsn(ATHROW);
    }

    private boolean isEvalMethod() {
        return "eval".equals(codeGen.getCtx().getMethodName()) && !codeGen.getCtx().isStatic();
    }

    @Override
    public void visitName(final QualifiedName name) {
        codeGen.visitName(name);
    }

    @Override
    public void visitExpressionFieldAccess(final ExpressionFieldAccess fa) {
        codeGen.visitExpressionFieldAccess(fa);
    }

    @Override
    public void visitAnnotation(final Annotation annotation) {
        codeGen.visitAnnotation(annotation);
    }

    @Override
    public void visitField(final Field field) {
        if (isEvalMethod()) {
            field.getInitialValue().accept(this);
        } else {
            codeGen.visitField(field);
        }
    }

    @Override
    public void visitIf(final IfStatement ifStatement) {
        codeGen.visitIf(ifStatement);
    }

    @Override
    public void visitWhile(final WhileLoop whileLoop) {
        codeGen.visitWhile(whileLoop);
    }

    @Override
    public void visitBinaryOp(final BinaryOp binaryOp) {
        codeGen.visitBinaryOp(binaryOp);
    }

    @Override
    public void visitUnaryOp(final UnaryOp unaryOp) {
        codeGen.visitUnaryOp(unaryOp);
    }

    @Override
    public void visitLiteral(final Literal literal) {
        codeGen.visitLiteral(literal);
    }

    @Override
    public void visitCall(final Call call) {
        codeGen.visitCall(call);
    }

    @Override
    public void visitReturn(final Return returnStmt) {
        codeGen.visitReturn(returnStmt);
    }

    @Override
    public void visitFor(final ForLoop forLoop) {
        codeGen.visitFor(forLoop);
    }

    @Override
    public void visitForEach(final ForEachLoop forEachLoop) {
        codeGen.visitForEach(forEachLoop);
    }

    @Override
    public void visitTypeCast(final TypeCast cast) {
        codeGen.visitTypeCast(cast);
    }

    @Override
    public void visitAssignment(final Assignment assignment) {
        codeGen.visitAssignment(assignment);
    }

    @Override
    public void visitNew(final New newExpr) {
        codeGen.visitNew(newExpr);
    }

    @Override
    public void visitExpressionIndices(final ExpressionIndex expressionIndex) {
        codeGen.visitExpressionIndices(expressionIndex);
    }
}
