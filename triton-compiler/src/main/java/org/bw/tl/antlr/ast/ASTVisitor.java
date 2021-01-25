package org.bw.tl.antlr.ast;

public interface ASTVisitor {

    void visitName(final QualifiedName name);

    void visitExpressionFieldAccess(final ExpressionFieldAccess fa);

    void visitAnnotation(final Annotation annotation);

    void visitField(final Field field);

    void visitFunction(final Function function);

    void visitIf(final IfStatement ifStatement);

    void visitWhile(final WhileLoop whileLoop);

    void visitBinaryOp(final BinaryOp binaryOp);

    void visitUnaryOp(final UnaryOp unaryOp);

    void visitLiteral(final Literal literal);

    void visitCall(final Call call);

    void visitReturn(final Return returnStmt);

    void visitFor(final ForLoop forLoop);

    void visitForEach(final ForEachLoop forEachLoop);

    void visitTypeCast(final TypeCast cast);

    void visitAssignment(final Assignment assignment);

    void visitNew(final New newExpr);

    void visitWhen(final When when);

    void visitScheduleBlock(final ScheduleBlock task);

    void visitExpressionIndices(final ExpressionIndex expressionIndex);

    void visitTask(final Task task);

    void visitRmdDelegate(final RmdDelegate delegate);

    void visitRmdAsyncDelegate(final RmdAsyncDelegate delegate);

}
