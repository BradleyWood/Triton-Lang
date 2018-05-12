package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Literal;

@RequiredArgsConstructor(staticName = "of")
public class LiteralVisitor extends GrammarBaseVisitor<Literal> {

    private final @Getter String sourceFile;

    @Override
    public Literal visitLiteral(final GrammarParser.LiteralContext ctx) {
        Literal literal = null;

        if (ctx.NULL() != null) {
            literal = new Literal<>(null);
        } else if (ctx.bool() != null) {
            literal = new Literal<>(Boolean.parseBoolean(ctx.bool().getText()));
        } else if (ctx.string() != null) {
            final String quote = ctx.string().getText();
            literal = new Literal<>(quote.substring(1, quote.length() - 1));
        } else if (ctx.number() != null) {
            final int radix = ctx.number().HEX() != null ? 16 : 10;
            final Object value;
            if (ctx.number().FLOAT() == null) {
                value = Integer.parseInt(ctx.number().getText(), radix);
            } else {
                value = Double.parseDouble(ctx.number().getText());
            }
            literal = new Literal<>(value);
        }

        if (literal == null)
            throw new NullPointerException();

        literal.setFile(sourceFile);
        literal.setText(ctx.getText());
        literal.setLineNumber(ctx.start.getLine());

        return literal;
    }
}
