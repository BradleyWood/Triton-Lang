package org.bw.tl.compiler.resolve;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.bw.tl.antlr.GrammarLexer;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.File;
import org.bw.tl.antlr.ast.Module;
import org.bw.tl.antlr.visitor.FileVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.util.Collections;

public class SymbolResolutionTest {

    static SymbolResolver getResolver(final String txt) {
        final GrammarLexer lexer = new GrammarLexer(CharStreams.fromString(txt));
        final CommonTokenStream ts = new CommonTokenStream(lexer);
        final GrammarParser p = new GrammarParser(ts);

        p.removeErrorListener(ConsoleErrorListener.INSTANCE);

        GrammarParser.FileContext fc = p.file();

        if (p.getNumberOfSyntaxErrors() == 0) {
            final File file = fc.accept(FileVisitor.of("<test>"));
            System.out.println("file: " + file);
            final Module mod = Module.of(file);
            return new SymbolResolver(Collections.singletonList(mod), mod);
        }
        return null;
    }

    @Test
    public void testResolveFunction() {
        final SymbolResolver resolver = getResolver("package testModule; int testFunction() {}" +
                "void anotherTest() {} \n" +
                "void funWithParams(boolean a, int b) {}\n");

        final Type modType = Type.getType("LtestModule;");

        Assert.assertEquals(Type.INT_TYPE, resolver.resolveFunction(modType, "testFunction"));
        Assert.assertEquals(Type.VOID_TYPE, resolver.resolveFunction(modType, "anotherTest"));

        Assert.assertEquals(Type.VOID_TYPE, resolver.resolveFunction(modType, "funWithParams", Type.BOOLEAN_TYPE,
                Type.INT_TYPE));

        Assert.assertNull(resolver.resolveFunction(modType, "funWithParams", Type.BOOLEAN_TYPE,
                Type.LONG_TYPE));
    }
}
