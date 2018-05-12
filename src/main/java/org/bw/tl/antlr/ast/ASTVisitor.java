package org.bw.tl.antlr.ast;

public interface ASTVisitor {

    void visitName(final QualifiedName name);

    void visitAnnotation(final Annotation annotation);

    void visitField(final Field field);

}
