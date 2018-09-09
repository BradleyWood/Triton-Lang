package org.bw.tl.antlr.visitor;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.Node;
import org.bw.tl.antlr.ast.When;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(staticName = "of")
public class WhenVisitor extends GrammarBaseVisitor<When> {

    private final @Getter String sourceFile;

    @Override
    public When visitWhenExpr(final GrammarParser.WhenExprContext ctx) {
        final Expression expr = ctx.expression().accept(ExpressionVisitor.of(sourceFile));
        final List<Expression> conditions = new LinkedList<>();
        final List<Node> branches = new LinkedList<>();
        Node elseBranch = null;

        if (ctx.whenCase() != null) {
            for (final GrammarParser.WhenCaseContext whenCaseContext : ctx.whenCase()) {
                if (whenCaseContext.whenCondition().expression() != null) {
                    conditions.add(whenCaseContext.whenCondition().expression().accept(ExpressionVisitor.of(sourceFile)));
                } else {
                    throw new UnsupportedOperationException("Instance check not supported yet for 'when' expression");
                }

                if (whenCaseContext.expression() != null) {
                    branches.add(whenCaseContext.expression().accept(ExpressionVisitor.of(sourceFile)));
                } else if (whenCaseContext.block() != null) {
                    branches.add(whenCaseContext.block().accept(BlockVisitor.of(sourceFile)));
                }
            }
        }

        if (ctx.whenElse() != null) {
            if (ctx.whenElse().block() != null) {
                elseBranch = ctx.whenElse().block().accept(BlockVisitor.of(sourceFile));
            } else if (ctx.whenElse().expression() != null) {
                elseBranch = ctx.whenElse().expression().accept(ExpressionVisitor.of(sourceFile));
            }
        }

        final When when = new When(expr, conditions, branches, elseBranch);

        expr.setParent(when);
        conditions.forEach(e -> e.setParent(when));
        branches.forEach(n -> n.setParent(when));

        if (elseBranch != null)
            elseBranch.setParent(when);

        return when;
    }
}
