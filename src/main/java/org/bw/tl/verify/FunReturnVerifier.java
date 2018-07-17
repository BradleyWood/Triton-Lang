package org.bw.tl.verify;

import org.bw.tl.antlr.ast.*;

import java.util.List;

/**
 * Verifies that a function has a return statement
 *
 */
public class FunReturnVerifier implements Verifiable<Function> {

    @Override
    public boolean isValid(final Function fun) {
        if (fun.getType().getName().equals("void"))
            return true;

        final NodeVisitor nv = new NodeVisitor();
        fun.accept(nv);

        return nv.isValid;
    }

    class NodeVisitor implements ASTVisitor {

        private boolean isValid = false;

        @Override
        public void visitFunction(final Function function) {
            final List<Node> block = function.getBody().getStatements();

            if (block.isEmpty())
                return;

            isValid = handleBlock(block);
        }

        private boolean handleBlock(final List<Node> stmts) {
            if (stmts.isEmpty())
                return false;

            final Node last = stmts.get(stmts.size() - 1);

            if (last instanceof Return) {
                return true;
            } else if (last instanceof Block){
                return handleBlock(((Block) last).getStatements());
            } else {
                last.accept(this);
                return isValid;
            }
        }

        @Override
        public void visitIf(final IfStatement ifStatement) {
            final Node body = ifStatement.getBody();
            final Node elseBody = ifStatement.getElseBody();

            if (body instanceof Return) {
                if (elseBody instanceof Return || elseBody == null) {
                    isValid = true;
                } else if (elseBody instanceof Block) {
                    isValid = handleBlock(((Block) elseBody).getStatements());
                } else {
                    elseBody.accept(this);
                }
            } else if (body instanceof Block && handleBlock(((Block)body).getStatements())) {
                if (elseBody instanceof Return || elseBody == null) {
                    isValid = true;
                } else if (elseBody instanceof Block) {
                    isValid = handleBlock(((Block) elseBody).getStatements());
                } else {
                    elseBody.accept(this);
                }
            } else {
                body.accept(this);
            }
        }

        @Override
        public void visitFor(final ForLoop forLoop) {
        }

        @Override
        public void visitTypeCast(TypeCast cast) {
        }

        @Override
        public void visitName(QualifiedName name) {
        }

        @Override
        public void visitAnnotation(Annotation annotation) {
        }

        @Override
        public void visitField(Field field) {
        }

        @Override
        public void visitAssignment(Assignment assignment) {
        }

        @Override
        public void visitNew(New newExpr) {
        }

        @Override
        public void visitWhile(final WhileLoop whileLoop) {
        }

        @Override
        public void visitBinaryOp(BinaryOp binaryOp) {
        }

        @Override
        public void visitUnaryOp(UnaryOp unaryOp) {
        }

        @Override
        public void visitLiteral(Literal literal) {
        }

        @Override
        public void visitCall(Call call) {
        }

        @Override
        public void visitReturn(Return returnStmt) {
        }
    }
}
