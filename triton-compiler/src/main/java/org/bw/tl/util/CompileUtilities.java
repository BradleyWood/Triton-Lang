package org.bw.tl.util;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.bw.tl.Error;
import org.bw.tl.antlr.GrammarLexer;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Clazz;
import org.bw.tl.antlr.ast.QualifiedName;
import org.bw.tl.antlr.visitor.FileVisitor;
import org.bw.tl.compiler.Compiler;
import org.bw.tl.primer.ModifierPrimer;
import org.bw.tl.primer.Primer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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

    public static final List<String> DEFAULT_IMPORTS = Arrays.asList(
            "java.lang.System",
            "java.lang.Throwable",
            "java.lang.Thread",
            "java.lang.Object",
            "java.lang.Class",
            "java.lang.String",
            "java.lang.Math",
            "java.lang.StrictMath",
            "java.lang.StackTraceElement",
            "java.lang.SecurityManager",
            "java.lang.Runtime",
            "java.lang.Iterable",
            "java.lang.Comparable",
            "java.lang.CharSequence",
            "java.lang.Cloneable",
            "java.lang.Number",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.Void"
    );

    @Nullable
    public static Map<String, byte[]> compile(@NotNull final String directory, @NotNull final String... classpath)
            throws IOException {
        return compile(directory, Arrays.asList(classpath));
    }

    @Nullable
    public static Map<String, byte[]> compile(@NotNull final String directory, @NotNull final List<String> classpath)
            throws IOException {
        final List<String> files = Files.walk(Paths.get(directory)).filter(path -> path.toString().endsWith(FILE_EXTENSION))
                .map(Path::toString).collect(Collectors.toList());

        return compile(files, classpath);
    }

    @Nullable
    public static Map<String, byte[]> compile(@NotNull final List<String> files, @NotNull final String... classpath)
            throws IOException {
        return compile(files, Arrays.asList(classpath));
    }

    @Nullable
    public static Map<String, byte[]> compile(@NotNull final List<String> files, @NotNull final List<String> classpath)
            throws IOException {

        for (final String path : classpath) {
            if (!addToClasspath(path)) {
                System.err.println("Failure reading: " + path);
                return null;
            }
        }

        final List<Clazz> classes = new LinkedList<>();

        for (final String file : files) {
            final Clazz cl = getClazz(file);
            if (cl == null)
                return null;

            DEFAULT_IMPORTS.forEach(imp -> cl.getImports().add(QualifiedName.of(imp)));
            PRIMERS.forEach(p -> p.prime(cl));
            classes.add(cl);
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

    private static boolean addToClasspath(@NotNull final String s) throws IOException {
        return addToClasspath(new File(s).toURI().toURL());
    }

    private static boolean addToClasspath(@NotNull final URL u) {
        if (!(ClassLoader.getSystemClassLoader() instanceof URLClassLoader)) {
            return false;
        } else if (addUrlMethod == null) {
            try {
                addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addUrlMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
        if (addUrlMethod == null) {
            return false;
        }

        try {
            addUrlMethod.invoke(ClassLoader.getSystemClassLoader(), u);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
        return true;
    }

    private static Method addUrlMethod = null;
}
