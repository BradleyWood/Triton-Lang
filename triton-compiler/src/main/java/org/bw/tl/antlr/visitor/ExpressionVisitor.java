package org.bw.tl.antlr.visitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(staticName = "of")
public class ExpressionVisitor extends GrammarBaseVisitor<Expression> {

    private final @Getter String sourceFile;

    @Override
    public Expression visitExpression(final GrammarParser.ExpressionContext ctx) {
        Expression expression = null;

        if (ctx.wrapped != null) {
            expression = ctx.wrapped.accept(this);
        } else if (ctx.literal() != null) {
            expression = ctx.literal().accept(LiteralVisitor.of(sourceFile));
        } else if (ctx.id != null) {
            final Expression preceding = ctx.expression(0).accept(this);
            expression = new ExpressionFieldAccess(preceding, ctx.id.getText());
            preceding.setParent(expression);
        } else if (ctx.name != null) {
            expression = ctx.name.accept(FQNVisitor.of(sourceFile));
        } else if (ctx.call != null) {
            final Expression precedingExpr = ctx.preceeding != null ? ctx.preceeding.accept(this) : null;
            final String name = ctx.call.IDENTIFIER().getText();

            final List<Expression> expressionList = ctx.call.expression().stream().map(ec -> ec.accept(this))
                    .collect(Collectors.toList());

            expression = new Call(precedingExpr, name, expressionList);

            if (precedingExpr != null) {
                precedingExpr.setParent(expression);
            }
            for (final Expression expr : expressionList) {
                expr.setParent(expression);
            }
        } else if (ctx.newStatement() != null) {
            expression = ctx.newStatement().accept(NewVisitor.of(sourceFile));
        } else if (ctx.whenExpr() != null) {
            expression = ctx.whenExpr().accept(WhenVisitor.of(sourceFile));
        } else if (ctx.ifStatement() != null) {
            expression = ctx.ifStatement().accept(IfVisitor.of(sourceFile));
        } else if(ctx.indices() != null) {
            final Expression lstMapOrArray = ctx.expression(0).accept(this);
            final List<Expression> indices = new LinkedList<>();
            final Expression value = ctx.assign != null ? ctx.assign.accept(this) : null;
            final ExpressionIndex eIdx = new ExpressionIndex(lstMapOrArray, indices, value);
            lstMapOrArray.setParent(eIdx);
            ctx.indices().expression().stream().map(e -> e.accept(this)).peek(e -> e.setParent(eIdx))
                    .forEach(indices::add);
            expression = eIdx;
        } else if(ctx.typeCast() != null) {
            expression = ctx.typeCast().accept(TypeCastVisitor.of(sourceFile));
        } else if (ctx.lhs != null && ctx.rhs != null) {
            final int start = ctx.lhs.getText().length();
            final int end = ctx.getText().length() - ctx.rhs.getText().length();
            final String operator = ctx.getText().substring(start, end);
            final Expression lhs = ctx.lhs.accept(this);
            final Expression rhs = ctx.rhs.accept(this);

            expression = new BinaryOp(lhs, operator, rhs);
            lhs.setParent(expression);
            rhs.setParent(expression);
        } else if (ctx.unaryOperand != null) {
            final int end = ctx.getText().length() - ctx.unaryOperand.getText().length();
            final Expression uExpr = ctx.unaryOperand.accept(this);

            expression = new UnaryOp(uExpr, ctx.getText().substring(0, end));
            uExpr.setParent(expression);
        } else if (ctx.assignment() != null) {
            final Expression lhs = ctx.preceeding != null ? ctx.preceeding.accept(this) : null;
            Expression rhs = ctx.assignment().val.accept(this);

            final int start = ctx.assignment().IDENTIFIER().getText().length();
            final int end = ctx.assignment().getText().length() - ctx.assignment().val.getText().length();
            final String op = ctx.assignment().getText().substring(start, end);
            if (!op.equals("=")) {
                final Expression fa = lhs != null ? new ExpressionFieldAccess(lhs, ctx.assignment().IDENTIFIER().getText()) :
                        QualifiedName.of(ctx.assignment().IDENTIFIER().getText());

                rhs = new BinaryOp(fa, op.substring(0, 1), rhs);
            }

            expression = new Assignment(lhs, ctx.assignment().IDENTIFIER().getText(), rhs);

            if (lhs != null)
                lhs.setParent(expression);
            rhs.setParent(expression);
        }

        if (expression == null)
            throw new NullPointerException();

        expression.setFile(sourceFile);
        expression.setText(ctx.getText());
        expression.setLineNumber(ctx.start.getLine());

        return expression;
    }
}
