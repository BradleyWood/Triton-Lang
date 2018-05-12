package org.bw.tl.antlr;

import lombok.Data;
import org.antlr.v4.runtime.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public @Data class RuleTest {

    private final String txt;
    private final String file;
    private final boolean isTopLevel;
    private final boolean pass;

    @Test
    public void testSyntax() {
        if (txt == null || txt.isEmpty())
            Assert.fail("No input text for " + file);

        final GrammarLexer lexer = new GrammarLexer(CharStreams.fromString(txt));
        final CommonTokenStream ts = new CommonTokenStream(lexer);
        final GrammarParser p = new GrammarParser(ts);

        p.removeErrorListener(ConsoleErrorListener.INSTANCE);

        try {
            if (!isTopLevel) {
                new GrammarBaseVisitor().visitStatement(p.statement());
            } else {
                new GrammarBaseVisitor().visitFile(p.file());
            }
        } catch (Exception e) {
            if (pass) {
                Assert.fail();
            } else {
                return;
            }
        }

        if (pass) {
            Assert.assertEquals(0, p.getNumberOfSyntaxErrors());
        } else {
            Assert.assertNotEquals(0, p.getNumberOfSyntaxErrors());
        }
    }


    private static String load(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            return null;
        }
    }

    @Parameterized.Parameters(name = "Grammar Test {1} ")
    public static Collection getParameters() throws IOException {
        return Files.walk(Paths.get("testData/grammar"))
                .filter(path -> path.toString().toLowerCase().endsWith(".gr"))
                .map(path -> new Object[]{
                        load(path),
                        path.getFileName().toString(),
                        path.toString().toLowerCase().contains("top"),
                        path.toString().toLowerCase().contains("pass")
                }).collect(Collectors.toList());
    }
}
