package org.bw.tl.antlr.visitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Block;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.RmdAsyncDelegate;

@AllArgsConstructor(staticName = "of")
public class RmdAsyncVisitor extends GrammarBaseVisitor<RmdAsyncDelegate> {

    private final @Getter String sourceFile;

    @Override
    public RmdAsyncDelegate visitAsync(final GrammarParser.AsyncContext ctx) {
        final Block body = ctx.body.accept(BlockVisitor.of(sourceFile));
        Block callback = null;
        Expression condition = null;

        if (ctx.cb != null) {
            callback = ctx.cb.accept(BlockVisitor.of(sourceFile));
        }

        if (ctx.condition != null) {
            condition = ctx.condition.accept(ExpressionVisitor.of(sourceFile));
        }

        final RmdAsyncDelegate delegate = new RmdAsyncDelegate(body, callback, condition);
        body.setParent(delegate);

        if (callback != null) {
            callback.setParent(delegate);
        }

        if (condition != null) {
            condition.setParent(delegate);
        }

        return delegate;
    }
}
