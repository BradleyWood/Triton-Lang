package org.bw.tl.compiler;

import lombok.Data;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.resolve.FieldContext;

import java.util.LinkedList;
import java.util.List;

@Data
public class AnonymousFunctionVisitor implements ASTVisitor {

    private final List<FieldContext> params = new LinkedList<>();
    private final MethodCtx ctx;
    private final int outerScope;

    @Override
    public void visitName(final QualifiedName name) {
        final FieldContext[] ctxList = ctx.getResolver().resolveFieldCtx(name);

        if (ctxList != null && ctxList.length > 0) {
            if (ctxList[0].isLocal()) {
                final Scope.Var var = ctx.getScope().findVar(ctxList[0].getName());

                if (var.getScope() <= outerScope) {
                    params.add(ctxList[0]);
                }
            }
        }
    }

    @Override
    public void visitExpressionFieldAccess(final ExpressionFieldAccess fa) {
        if (fa.getPrecedingExpr() != null) {
            fa.getPrecedingExpr().accept(this);
        }
    }

    @Override
    public void visitField(final Field field) {
        if (field.getInitialValue() != null) {
            field.getInitialValue().accept(this);
        }
    }

    @Override
    public void visitTask(final Task task) {
        task.getBody().accept(this);

        for (final Constraint constraint : task.getConstraints()) {
            constraint.getConstraint().accept(this);

            if (constraint.getConstraintViolation() != null) {
                constraint.getConstraintViolation().accept(this);
            }
        }
    }

    @Override
    public void visitIf(final IfStatement ifStatement) {
        ifStatement.getCondition().accept(this);
        ifStatement.getBody().accept(this);

        if (ifStatement.getElseBody() != null) {
            ifStatement.getElseBody().accept(this);
        }
    }

    @Override
    public void visitWhile(final WhileLoop whileLoop) {
        whileLoop.getCondition().accept(this);
        whileLoop.getBody().accept(this);
    }

    @Override
    public void visitBinaryOp(final BinaryOp binaryOp) {
        binaryOp.getLeftSide().accept(this);
        binaryOp.getRightSide().accept(this);
    }

    @Override
    public void visitUnaryOp(final UnaryOp unaryOp) {
        unaryOp.getExpression().accept(this);
    }

    @Override
    public void visitCall(final Call call) {
        if (call.getPrecedingExpr() != null) {
            call.getPrecedingExpr().accept(this);
        }

        call.getParameters().forEach(param -> param.accept(this));
    }

    @Override
    public void visitAnnotation(final Annotation annotation) {
    }

    @Override
    public void visitFunction(final Function function) {
    }

    @Override
    public void visitLiteral(final Literal literal) {
    }

    @Override
    public void visitScheduleBlock(final ScheduleBlock task) {
    }

    @Override
    public void visitReturn(final Return returnStmt) {
        if (returnStmt.getExpression() != null) {
            returnStmt.getExpression().accept(this);
        }
    }

    @Override
    public void visitFor(final ForLoop forLoop) {
        if (forLoop.getInit() != null) {
            forLoop.getInit().accept(this);
        }

        if (forLoop.getCondition() != null) {
            forLoop.getCondition().accept(this);
        }

        forLoop.getUpdate().forEach(u -> u.accept(this));
        forLoop.getBody().accept(this);
    }

    @Override
    public void visitForEach(final ForEachLoop forEachLoop) {
        forEachLoop.getIterableExpression().accept(this);
        forEachLoop.getBody().accept(this);
    }

    @Override
    public void visitTypeCast(final TypeCast cast) {
        cast.getExpression().accept(this);
    }

    @Override
    public void visitAssignment(final Assignment assignment) {
        if (assignment.getPrecedingExpr() != null) {
            assignment.getPrecedingExpr().accept(this);
        }

        assignment.getValue().accept(this);
    }

    @Override
    public void visitNew(final New newExpr) {
        newExpr.getParameters().forEach(param -> param.accept(this));
    }

    @Override
    public void visitWhen(final When when) {
        when.getCases().forEach(c -> {
            c.getCondition().accept(this);
            c.getBranch().accept(this);
        });

        if (when.getData() != null) {
            when.getData().accept(this);
        }

        if (when.getElseBranch() != null) {
            when.getElseBranch().accept(this);
        }
    }

    @Override
    public void visitExpressionIndices(final ExpressionIndex expressionIndex) {
        expressionIndex.accept(this);
        expressionIndex.getIndices().forEach(index -> index.accept(this));
        expressionIndex.getValue().accept(this);
    }
}
