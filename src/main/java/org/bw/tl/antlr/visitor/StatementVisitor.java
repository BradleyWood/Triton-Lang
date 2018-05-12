package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Node;

@RequiredArgsConstructor(staticName = "of")
public class StatementVisitor extends GrammarBaseVisitor<Node> {

    private final @Getter String sourceFile;

    @Override
    public Node visitStatement(final GrammarParser.StatementContext ctx) {
        Node stmt = null;
        if (ctx.block() != null) {
            stmt = ctx.block().accept(BlockVisitor.of(sourceFile));
        } else if (ctx.ifStatement() != null) {

        } else if (ctx.whileStatement() != null) {

        } else if (ctx.forStatement() != null) {

        } else if (ctx.expression() != null) {

        } else if (ctx.varDef() != null) {
            stmt = ctx.varDef().accept(FieldVisitor.of(sourceFile));
        }

        if (stmt == null)
            throw new IllegalStateException();

        stmt.setLineNumber(ctx.start.getLine());
        stmt.setText(ctx.getText());
        stmt.setFile(sourceFile);

        return stmt;
    }
}
