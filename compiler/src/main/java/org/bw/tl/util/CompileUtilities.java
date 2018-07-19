package org.bw.tl.util;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.bw.tl.Error;
import org.bw.tl.antlr.GrammarLexer;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Clazz;
import org.bw.tl.antlr.visitor.FileVisitor;
import org.bw.tl.compiler.Compiler;
import org.bw.tl.primer.ModifierPrimer;
import org.bw.tl.primer.Primer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompileUtilities {

    public static final String FILE_EXTENSION = ".tl";

    public static final List<Primer> PRIMERS = Arrays.asList(new ModifierPrimer());

    @Nullable
    public static Map<String, byte[]> compile(@NotNull final String directory) throws IOException {
        final List<String> files = Files.walk(Paths.get(directory)).filter(path -> path.toString().endsWith(FILE_EXTENSION))
                .map(Path::toString).collect(Collectors.toList());

        return compile(files);
    }

    @Nullable
    public static Map<String, byte[]> compile(@NotNull final List<String> files) throws IOException {
        final List<Clazz> classes = new LinkedList<>();

        for (final String file : files) {
            final Clazz cl = getClazz(file);
            if (cl == null)
                return null;

            PRIMERS.forEach(p -> p.prime(cl));
        }

        final Compiler compiler = new Compiler(classes);

        final Map<String, byte[]> result = compiler.compile();

        final List<Error> errors = compiler.getErrors();

        if (!errors.isEmpty())
            System.err.println("Compilation failed with " + errors.size() + " errors.");

        for (final Error error : errors) {
            error.print();
        }

        return result;
    }

    @Nullable
    public static Clazz getClazz(@NotNull final String srcFile) throws IOException {
        final GrammarLexer lexer = new GrammarLexer(CharStreams.fromFileName(srcFile));
        final CommonTokenStream ts = new CommonTokenStream(lexer);
        final GrammarParser p = new GrammarParser(ts);

        GrammarParser.FileContext fc = p.file();

        if (p.getNumberOfSyntaxErrors() == 0) {
            return fc.accept(FileVisitor.of(srcFile));
        }

        return null;
    }
}
