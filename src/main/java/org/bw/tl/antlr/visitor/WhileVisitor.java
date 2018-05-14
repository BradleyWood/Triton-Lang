package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.Node;
import org.bw.tl.antlr.ast.WhileLoop;

@RequiredArgsConstructor(staticName = "of")
public class WhileVisitor extends GrammarBaseVisitor<WhileLoop> {

    private final @Getter String sourceFile;

    @Override
    public WhileLoop visitWhileStatement(final GrammarParser.WhileStatementContext ctx) {
        final Expression condition = ctx.condition.accept(ExpressionVisitor.of(sourceFile));
        final Node body = ctx.body.accept(StatementVisitor.of(sourceFile));
        final boolean doWhile = ctx.DO() != null;

        final WhileLoop whileLoop = new WhileLoop(condition, body, doWhile);

        body.setParent(whileLoop);
        condition.setParent(whileLoop);

        whileLoop.setText(ctx.getText());
        whileLoop.setLineNumber(ctx.start.getLine());
        whileLoop.setFile(sourceFile);

        return whileLoop;
    }
}
