package org.bw.tl;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.bw.tl.antlr.GrammarLexer;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.visitor.ExpressionVisitor;
import org.jetbrains.annotations.Nullable;

public class TestUtilities {

    @Nullable
    public static Expression parseExpression(final String expr) {
        final GrammarLexer lexer = new GrammarLexer(CharStreams.fromString(expr));
        final CommonTokenStream ts = new CommonTokenStream(lexer);
        final GrammarParser p = new GrammarParser(ts);

        p.removeErrorListener(ConsoleErrorListener.INSTANCE);

        final GrammarParser.ExpressionContext ctx = p.expression();

        if (p.getNumberOfSyntaxErrors() == 0) {
            return ctx.accept(ExpressionVisitor.of("<test>"));
        }

        return null;
    }
}
