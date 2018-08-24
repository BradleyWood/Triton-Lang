package org.triton.iterpreter;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.bw.tl.Error;
import org.bw.tl.antlr.GrammarLexer;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.util.CompileUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.triton.antlr.ast.Script;
import org.triton.antlr.visitor.ScriptVisitor;
import org.triton.compiler.ScriptCompiler;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public @Data class TritonInterpreter {

    private ScriptContext ctx;

    public TritonInterpreter() {
        this(new SimpleScriptContext());
    }

    public Object eval(@NotNull final String s) throws ScriptException {
        return compile(s, "<TritonScript>").eval(ctx);
    }

    public Object eval(@NotNull final Reader reader) throws ScriptException, IOException {
        return compile(reader).eval(ctx);
    }

    public void exec(@NotNull final String s) throws ScriptException {
        compile(s, "<TritonScript>").eval(ctx);
    }

    public void exec(@NotNull final Reader reader) throws IOException, ScriptException {
        compile(reader).eval(ctx);
    }

    public void execFile(@NotNull final String sourceFile) throws IOException, ScriptException {
        compile(sourceFile).eval(ctx);
    }

    private void exec(@NotNull final CompiledScript compiledScript) throws ScriptException {
        compiledScript.eval(ctx);
    }

    public void setAttribute(@NotNull final String name, final Object obj) {
        ctx.setAttribute(name, obj, ScriptContext.ENGINE_SCOPE);
    }

    public Object getAttribute(@NotNull final String name) {
        return ctx.getAttribute(name);
    }

    private static CompiledScript compile(@NotNull final String s, @NotNull final String source)
            throws ScriptException {
        return compile(CharStreams.fromString(s), source);
    }

    private static CompiledScript compile(@NotNull final Reader reader) throws IOException, ScriptException {
        return compile(CharStreams.fromReader(reader), "<TritonScript>");
    }

    private static CompiledScript compile(@NotNull final String srcFile) throws IOException, ScriptException {
        return compile(CharStreams.fromFileName(srcFile), srcFile);
    }

    private static CompiledScript compile(@NotNull final CharStream charStream, @NotNull final String sourceFile)
            throws ScriptException {
        final Script script = parseScript(charStream, sourceFile);

        if (script == null)
            return null;

        final Clazz clazz = buildTree(script);

        final ScriptCompiler sc = new ScriptCompiler(clazz);
        final String scriptName = "TritonScript$" + counter++;

        final byte[] bytes = sc.build(scriptName);

        if (bytes == null) {
            final List<Error> errors = sc.getErrors();

            if (!errors.isEmpty())
                throw new ScriptException(errors.get(0).getMessage());

            throw new ScriptException("Compilation failed");
        }

        final ClassLoader cl = new ClassLoader() {
            @Override
            public Class<?> findClass(String name) {
                return defineClass(name, bytes, 0, bytes.length);
            }
        };

        try {
            return (CompiledScript) cl.loadClass(scriptName).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new ScriptException("Internal Error");
        }
    }

    @Nullable
    private static Script parseScript(@NotNull final CharStream stream, @NotNull final String srcFile) {
        final GrammarLexer lexer = new GrammarLexer(stream);
        final CommonTokenStream ts = new CommonTokenStream(lexer);
        final GrammarParser p = new GrammarParser(ts);

        GrammarParser.ScriptContext scriptContext = p.script();

        if (p.getNumberOfSyntaxErrors() == 0) {
            return scriptContext.accept(ScriptVisitor.of(srcFile));
        }

        return null;
    }

    private static Clazz buildTree(@NotNull final Script script) {
        final List<QualifiedName> staticImports = new LinkedList<>();
        final List<QualifiedName> imports = script.getImports();

        CompileUtilities.DEFAULT_IMPORTS.forEach(imp -> imports.add(QualifiedName.of(imp)));
        CompileUtilities.DEFAULT_STATIC_IMPORTS.forEach(imp -> staticImports.add(QualifiedName.of(imp)));

        return new Clazz(QualifiedName.of("script"), imports, staticImports, Collections.emptyList(),
                Arrays.asList(buildEvalFunction(script.getStatements()), buildGetEngineFunction()), script.getSrcFile());
    }

    private static Function buildGetEngineFunction() {
        final TypeName[] parameterTypes = new TypeName[0];
        final String[] parameterNames = new String[0];
        final List<Modifier>[] modifiers = new List[0];

        final Block body = new Block(Collections.singletonList(new Return(new Literal<>(null))));

        final Function function = new Function(parameterTypes, parameterNames, modifiers, "getEngine",
                body, TypeName.of("javax.script.ScriptEngine"));
        function.addModifiers(Modifier.PUBLIC);

        return function;
    }

    private static Function buildEvalFunction(@NotNull final List<Node> statements) {
        final TypeName[] parameterTypes = new TypeName[]{TypeName.of("javax.script.ScriptContext")};
        final String[] parameterNames = new String[]{" __ctx__ "};
        final List<Modifier>[] modifiers = new List[]{new LinkedList<Modifier>()};

        final Block body = new Block(statements);

        final Function function = new Function(parameterTypes, parameterNames, modifiers, "eval", body, TypeName.of("java.lang.Object"));
        function.addModifiers(Modifier.PUBLIC);

        return function;
    }

    private static int counter = 0;
}
