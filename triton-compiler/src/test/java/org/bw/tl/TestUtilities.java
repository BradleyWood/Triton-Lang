package org.bw.tl;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.bw.tl.antlr.GrammarLexer;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.Clazz;
import org.bw.tl.antlr.visitor.ExpressionVisitor;
import org.bw.tl.antlr.visitor.FileVisitor;
import org.bw.tl.compiler.Scope;

import org.bw.tl.compiler.resolve.ExpressionResolverImpl;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class TestUtilities {

    public static ExpressionResolverImpl getResolver(final String txt) {
        final Clazz clazz = getClazz(txt);

        if (clazz == null)
            return null;

        return new ExpressionResolverImpl(clazz, Collections.singletonList(clazz), new Scope());
    }

    public static Clazz getClazz(final String txt, final String srcFile) {
        final GrammarLexer lexer = new GrammarLexer(CharStreams.fromString(txt));
        final CommonTokenStream ts = new CommonTokenStream(lexer);
        final GrammarParser p = new GrammarParser(ts);

        p.removeErrorListener(ConsoleErrorListener.INSTANCE);

        GrammarParser.FileContext fc = p.file();

        if (p.getNumberOfSyntaxErrors() == 0) {
            return fc.accept(FileVisitor.of(srcFile));
        }
        return null;
    }

    public static Clazz getClazz(final String txt) {
        return getClazz(txt, "<test>");
    }

    public static Clazz getClazzFromFile(final String file) {
        try {
            return getClazz(new String(Files.readAllBytes(Paths.get(file))), file);
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
