package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Block;
import org.bw.tl.antlr.ast.Function;

@RequiredArgsConstructor(staticName = "of")
public class FunctionVisitor extends GrammarBaseVisitor<Function> {

    private final @Getter String sourceFile;

    @Override
    public Function visitFunctionDef(final GrammarParser.FunctionDefContext ctx) {
        final String type = ctx.type() != null ? ctx.type().getText() : ctx.VOID_T().getText();
        final Block block = ctx.block().accept(BlockVisitor.of(sourceFile));
        final String name = ctx.IDENTIFIER().getText();

        final Function function = new Function(name, block, type);

        function.setText(ctx.getText());
        function.setFile(sourceFile);
        function.setLineNumber(ctx.start.getLine());

        return function;
    }
}
