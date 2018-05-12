package org.bw.tl.antlr.visitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.*;

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
        } else if (ctx.name != null) {
            expression = ctx.name.accept(FQNVisitor.of(sourceFile));
        } else if (ctx.call != null) {
            final Expression precedingExpr = ctx.preceeding != null ? ctx.preceeding.accept(this) : null;
            final String name = ctx.call.IDENTIFIER().getText();

            final List<Expression> expressionList = ctx.call.expression().stream().map(ec -> ec.accept(this))
                    .collect(Collectors.toList());

            expression = new Call(precedingExpr, name, expressionList);
        } else if (ctx.lhs != null && ctx.rhs != null) {
            final int start = ctx.lhs.getText().length();
            final int end = ctx.getText().length() - 1 - ctx.rhs.getText().length();
            final String operator = ctx.getText().substring(start, end);

            expression = new BinaryOp(ctx.lhs.accept(this), operator, ctx.rhs.accept(this));
        } else if (ctx.unaryOperand != null) {
            final int end = ctx.getText().length() - 1 - ctx.unaryOperand.getText().length();

            expression = new UnaryOp(ctx.unaryOperand.accept(this), ctx.getText().substring(0, end));
        } else if (ctx.assignment() != null) {
            final QualifiedName lhs = ctx.assignment().fqn().accept(FQNVisitor.of(sourceFile));
            final Expression rhs = ctx.assignment().val.accept(this);

            final int start = ctx.assignment().fqn().getText().length();
            final int end = ctx.assignment().val.getText().length() - 1 - ctx.assignment().getText().length();
            expression = new BinaryOp(lhs, ctx.getText().substring(start, end), rhs);
        }

        if (expression == null)
            throw new NullPointerException();

        expression.setFile(sourceFile);
        expression.setText(ctx.getText());
        expression.setLineNumber(ctx.start.getLine());

        return expression;
    }
}
