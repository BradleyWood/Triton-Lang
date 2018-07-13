package org.bw.tl;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.bw.tl.antlr.GrammarLexer;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.File;
import org.bw.tl.antlr.ast.Module;
import org.bw.tl.antlr.visitor.ExpressionVisitor;
import org.bw.tl.antlr.visitor.FileVisitor;
import org.bw.tl.compiler.resolve.SymbolResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class TestUtilities {

    public static SymbolResolver getResolver(final String txt) {
        final Module module = getModule(txt);

        if (module == null)
            return null;

        return new SymbolResolver(Collections.singletonList(module), module);
    }

    public static Module getModule(final String txt) {
        final GrammarLexer lexer = new GrammarLexer(CharStreams.fromString(txt));
        final CommonTokenStream ts = new CommonTokenStream(lexer);
        final GrammarParser p = new GrammarParser(ts);

        p.removeErrorListener(ConsoleErrorListener.INSTANCE);

        GrammarParser.FileContext fc = p.file();

        if (p.getNumberOfSyntaxErrors() == 0) {
            final File file = fc.accept(FileVisitor.of("<test>"));
            return Module.of(file);
        }
        return null;
    }

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
