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
import java.util.*;

@AllArgsConstructor
public @Data class TritonInterpreter {

    private final List<QualifiedName> imports = new LinkedList<>();
    private final Map<String, TypeName> fields = new HashMap<>();

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

    public void eval(@NotNull final CompiledScript script) throws ScriptException {
        final Object eval = script.eval(ctx);

        if (eval != Void.TYPE) {
            System.out.println(eval);
        }
    }

    public void exec(@NotNull final String s) throws ScriptException {
        eval(compile(s, "<TritonScript>"));
    }

    public void exec(@NotNull final Reader reader) throws IOException, ScriptException {
        eval(compile(reader));
    }

    public void execFile(@NotNull final String sourceFile) throws IOException, ScriptException {
        eval(compile(sourceFile));
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

    private CompiledScript compile(@NotNull final String s, @NotNull final String source)
            throws ScriptException {
        return compile(CharStreams.fromString(s), source);
    }

    private CompiledScript compile(@NotNull final Reader reader) throws IOException, ScriptException {
        return compile(CharStreams.fromReader(reader), "<TritonScript>");
    }

    private CompiledScript compile(@NotNull final String srcFile) throws IOException, ScriptException {
        return compile(CharStreams.fromFileName(srcFile), srcFile);
    }

    private CompiledScript compile(@NotNull final CharStream charStream, @NotNull final String sourceFile)
            throws ScriptException {
        final Script script = parseScript(charStream, sourceFile);

        if (script == null)
            throw new ScriptException("Compilation failed: syntax error");

        final Clazz clazz = buildTree(script);

        final ScriptCompiler sc = new ScriptCompiler(clazz);
        final String scriptName = "TritonScript$" + counter++;

        final byte[] bytes = sc.build(scriptName, fields);

        if (bytes == null) {
            final List<Error> errors = sc.getErrors();

            if (!errors.isEmpty())
                throw new ScriptException(errors.get(0).toString());

            throw new ScriptException("Compilation failed");
        }

        final ClassLoader cl = new ClassLoader() {
            @Override
            public Class<?> findClass(String name) {
                return defineClass(name, bytes, 0, bytes.length);
            }
        };

        imports.addAll(script.getImports());

        try {
            final CompiledScript cs = (CompiledScript) cl.loadClass(scriptName).newInstance();

            script.getStatements().stream().filter(s -> s instanceof Field).map(s -> (Field) s).forEach(s -> {
                fields.put(s.getName(), s.getType());
            });

            return cs;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new ScriptException("Internal Error");
        }
    }

    @Nullable
    private static Script parseScript(@NotNull final CharStream stream, @NotNull final String srcFile) {
        final GrammarLexer lexer = new GrammarLexer(stream);
        final CommonTokenStream ts = new CommonTokenStream(lexer);
        final GrammarParser p = new GrammarParser(ts);

        try {
            GrammarParser.ScriptContext scriptContext = p.script();

            if (p.getNumberOfSyntaxErrors() == 0) {
                return scriptContext.accept(ScriptVisitor.of(srcFile));
            }
        } catch (Throwable e) {
        }

        return null;
    }

    private Clazz buildTree(@NotNull final Script script) {
        final List<QualifiedName> staticImports = new LinkedList<>();
        final List<QualifiedName> imports = script.getImports();

        CompileUtilities.DEFAULT_IMPORTS.forEach(imp -> imports.add(QualifiedName.of(imp)));
        imports.addAll(this.imports);
        CompileUtilities.DEFAULT_STATIC_IMPORTS.forEach(imp -> staticImports.add(QualifiedName.of(imp)));

        return new Clazz(QualifiedName.of("script"), imports, staticImports, Collections.emptyList(),
                Arrays.asList(buildEvalFunction(script.getStatements()), buildGetEngineFunction()),
                Collections.emptyList(), script.getSrcFile());
    }

    private Function buildGetEngineFunction() {
        final TypeName[] parameterTypes = new TypeName[0];
        final String[] parameterNames = new String[0];
        final List<Modifier>[] modifiers = new List[0];

        final Block body = new Block(Collections.singletonList(new Return(new Literal<>(null))));

        final Function function = new Function(parameterTypes, parameterNames, modifiers, "getEngine",
                body, TypeName.of("javax.script.ScriptEngine"));
        function.addModifiers(Modifier.PUBLIC);

        return function;
    }

    private Function buildEvalFunction(@NotNull final List<Node> statements) {
        final TypeName[] parameterTypes = new TypeName[]{TypeName.of("javax.script.ScriptContext")};
        final String[] parameterNames = new String[]{" __ctx__ "};
        final List<Modifier>[] modifiers = new List[]{new LinkedList<Modifier>()};

        final Block body = new Block(new ArrayList<>(statements));

        final Function function = new Function(parameterTypes, parameterNames, modifiers, "eval", body, TypeName.of("java.lang.Object"));
        function.addModifiers(Modifier.PUBLIC);

        return function;
    }

    private static int counter = 0;
}
