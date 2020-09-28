package org.bw.tl.antlr.ast;

public class ASTVisitorBase implements ASTVisitor {

    @Override
    public void visitName(final QualifiedName name) {
        throw new UnsupportedOperationException("Names not implemented");
    }

    @Override
    public void visitExpressionFieldAccess(final ExpressionFieldAccess fa) {
        throw new UnsupportedOperationException("Expression.FieldAccess not implemented");
    }

    @Override
    public void visitAnnotation(final Annotation annotation) {
        throw new UnsupportedOperationException("Annotations not implemented");
    }

    @Override
    public void visitField(final Field field) {
        throw new UnsupportedOperationException("Fields not implemented");
    }

    @Override
    public void visitFunction(final Function function) {
        throw new UnsupportedOperationException("Function not implemented");
    }

    @Override
    public void visitTask(final Task task) {
        throw new UnsupportedOperationException("Tasks not implemented");
    }

    @Override
    public void visitIf(final IfStatement ifStatement) {
        throw new UnsupportedOperationException("If statements not implemented");
    }

    @Override
    public void visitWhile(final WhileLoop whileLoop) {
        throw new UnsupportedOperationException("While loop not implemented");
    }

    @Override
    public void visitBinaryOp(final BinaryOp binaryOp) {
        throw new UnsupportedOperationException("Binary operators not implemented");
    }

    @Override
    public void visitUnaryOp(final UnaryOp unaryOp) {
        throw new UnsupportedOperationException("Unary operators not implemented");
    }

    @Override
    public void visitLiteral(final Literal literal) {
        throw new UnsupportedOperationException("Literal values not implemented");
    }

    @Override
    public void visitCall(final Call call) {
        throw new UnsupportedOperationException("Function calls not implemented");
    }

    @Override
    public void visitReturn(final Return returnStmt) {
        throw new UnsupportedOperationException("Return statement not implemented");
    }

    @Override
    public void visitScheduleBlock(final ScheduleBlock task) {
        throw new UnsupportedOperationException("Schedule block not implemented");
    }

    @Override
    public void visitFor(final ForLoop forLoop) {
        throw new UnsupportedOperationException("For loop not implemented");
    }

    @Override
    public void visitForEach(final ForEachLoop forEachLoop) {
        throw new UnsupportedOperationException("Foreach not implemented");
    }

    @Override
    public void visitTypeCast(TypeCast cast) {
        throw new UnsupportedOperationException("Type cast not implemented");
    }

    @Override
    public void visitAssignment(final Assignment assignment) {
        throw new UnsupportedOperationException("Assignment not implemented");
    }

    @Override
    public void visitNew(New newExpr) {
        throw new UnsupportedOperationException("New not implemented");
    }

    @Override
    public void visitWhen(When when) {
        throw new UnsupportedOperationException("When not implemented");
    }

    @Override
    public void visitExpressionIndices(final ExpressionIndex expressionIndex) {
        throw new UnsupportedOperationException("Expression indices not supported");
    }
}
