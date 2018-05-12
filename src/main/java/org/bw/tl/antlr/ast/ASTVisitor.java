package org.bw.tl.antlr.ast;

public interface ASTVisitor {

    void visitName(final QualifiedName name);

    void visitAnnotation(final Annotation annotation);

    void visitField(final Field field);

    void visitFunction(final Function function);

    void visitIf(final IfStatement ifStatement);

    void visitWhile(final WhileLoop whileLoop);

    void visitBinaryOp(final BinaryOp binaryOp);

    void visitLiteral(final Literal literal);

}
