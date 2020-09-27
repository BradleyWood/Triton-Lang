package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.*;

@RequiredArgsConstructor(staticName = "of")
public class StatementVisitor extends GrammarBaseVisitor<Node> {

    private final @Getter String sourceFile;

    @Override
    public Node visitStatement(final GrammarParser.StatementContext ctx) {
        Node stmt = null;
        if (ctx.block() != null) {
            stmt = ctx.block().accept(BlockVisitor.of(sourceFile));
        } else if (ctx.whileStatement() != null) {
            stmt = ctx.whileStatement().accept(WhileVisitor.of(sourceFile));
        } else if (ctx.forStatement() != null) {
            stmt = ctx.forStatement().accept(ForLoopVisitor.of(sourceFile));
        } else if (ctx.expression() != null) {
            stmt = ctx.expression().accept(ExpressionVisitor.of(sourceFile));
            ((Expression) stmt).setPop(true);
        } else if (ctx.varDef() != null) {
            stmt = ctx.varDef().accept(FieldVisitor.of(sourceFile));
        } else if (ctx.returnStatement() != null) {
            Expression retVal = null;

            if (ctx.returnStatement().expression() != null)
                retVal = ctx.returnStatement().expression().accept(ExpressionVisitor.of(sourceFile));

            stmt = new Return(retVal);

            if (retVal != null)
                retVal.setParent(stmt);
        } else if (ctx.SEMICOLON() != null) {
            stmt = new EmptyStatement();
        }

        if (stmt == null)
            throw new IllegalStateException();

        stmt.setLineNumber(ctx.start.getLine());
        stmt.setText(ctx.getText());
        stmt.setFile(sourceFile);

        return stmt;
    }
}
