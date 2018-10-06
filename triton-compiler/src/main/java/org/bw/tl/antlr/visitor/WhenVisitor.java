package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.Node;
import org.bw.tl.antlr.ast.When;
import org.bw.tl.antlr.ast.WhenCase;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(staticName = "of")
public class WhenVisitor extends GrammarBaseVisitor<When> {

    private final @Getter String sourceFile;

    @Override
    public When visitWhenExpr(final GrammarParser.WhenExprContext ctx) {
        final Expression expr = ctx.expression().accept(ExpressionVisitor.of(sourceFile));
        final List<WhenCase> cases = new LinkedList<>();
        Node elseBranch = null;

        if (ctx.whenElse() != null) {
            if (ctx.whenElse().block() != null) {
                elseBranch = ctx.whenElse().block().accept(BlockVisitor.of(sourceFile));
            } else if (ctx.whenElse().expression() != null) {
                elseBranch = ctx.whenElse().expression().accept(ExpressionVisitor.of(sourceFile));
            }
        }

        final When when = new When(expr, cases, elseBranch);

        if (ctx.whenCase() != null) {
            for (final GrammarParser.WhenCaseContext whenCaseContext : ctx.whenCase()) {
                final Expression branchCondition;
                final Node branch;

                if (whenCaseContext.whenCondition().expression() != null) {
                    branchCondition = whenCaseContext.whenCondition().expression().accept(ExpressionVisitor.of(sourceFile));
                } else {
                    throw new UnsupportedOperationException("Instance check not supported yet for 'when' expression");
                }

                if (whenCaseContext.expression() != null) {
                    branch = whenCaseContext.expression().accept(ExpressionVisitor.of(sourceFile));
                } else if (whenCaseContext.block() != null) {
                    branch = whenCaseContext.block().accept(BlockVisitor.of(sourceFile));
                } else {
                    throw new IllegalStateException("No branch for when case");
                }

                branch.setParent(when);
                branchCondition.setParent(when);

                cases.add(new WhenCase(branchCondition, branch));
            }
        }

        expr.setParent(when);

        if (elseBranch != null)
            elseBranch.setParent(when);

        return when;
    }
}
