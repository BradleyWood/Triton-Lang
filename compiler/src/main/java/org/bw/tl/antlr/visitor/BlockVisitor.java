package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Block;
import org.bw.tl.antlr.ast.Node;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(staticName = "of")
public class BlockVisitor extends GrammarBaseVisitor<Block> {

    private final @Getter String sourceFile;

    @Override
    public Block visitBlock(final GrammarParser.BlockContext ctx) {
        final List<Node> statements = new LinkedList<>();
        final Block block = new Block(statements);

        final StatementVisitor stmtVisitor = StatementVisitor.of(sourceFile);
        for (final GrammarParser.StatementContext stmtCtx : ctx.statement()) {
            final Node stmt = stmtCtx.accept(stmtVisitor);
            stmt.setParent(block);
            statements.add(stmt);
        }

        return block;
    }
}
