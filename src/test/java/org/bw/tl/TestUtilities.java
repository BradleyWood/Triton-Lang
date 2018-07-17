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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class TestUtilities {

    public static SymbolResolver getResolver(final String txt) {
        final Module module = getModule(txt);

        if (module == null)
            return null;

        return new SymbolResolver(Collections.singletonList(module), module, module.getFiles().get(0));
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

    public static Module getModuleFromFile(final String file) {
        try {
            return getModule(new String(Files.readAllBytes(Paths.get(file))));
        } catch (IOException e) {
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

    public static Class<?> loadClass(final String name, final byte[] bytes) {
        ClassLoader cl = new ClassLoader() {
            @Override
            public Class<?> findClass(String name) {
                return defineClass(name, bytes, 0, bytes.length);
            }
        };
        try {
            return cl.loadClass(name);
        } catch (final ClassNotFoundException ignored) {
        }
        return null;
    }
}
