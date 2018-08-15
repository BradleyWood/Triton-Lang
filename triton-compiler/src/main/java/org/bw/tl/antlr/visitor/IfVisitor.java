package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.IfStatement;
import org.bw.tl.antlr.ast.Node;

@RequiredArgsConstructor(staticName = "of")
public class IfVisitor extends GrammarBaseVisitor<IfStatement> {

    private final @Getter String sourceFile;

    @Override
    public IfStatement visitIfStatement(final GrammarParser.IfStatementContext ctx) {
        final StatementVisitor stmtVisitor = StatementVisitor.of(sourceFile);

        final Expression condition = ctx.condition.accept(ExpressionVisitor.of(sourceFile));
        final Node body = ctx.body.accept(stmtVisitor);
        Node otherwise = null;

        if (ctx.else_ != null)
            otherwise = ctx.else_.accept(stmtVisitor);

        final IfStatement stmt = new IfStatement(condition, body, otherwise);

        body.setParent(stmt);
        condition.setParent(stmt);

        if (otherwise != null)
            otherwise.setParent(stmt);

        stmt.setText(ctx.getText());
        stmt.setLineNumber(ctx.start.getLine());
        stmt.setFile(sourceFile);

        return stmt;
    }
}
