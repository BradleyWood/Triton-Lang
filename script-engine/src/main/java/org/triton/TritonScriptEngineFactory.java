package org.triton;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bw.tl.compiler.Compiler;

public class TritonScriptEngineFactory implements ScriptEngineFactory {

    @Override
    public String getEngineName() {
        return "triton";
    }

    @Override
    public String getEngineVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public List<String> getExtensions() {
        return Collections.unmodifiableList(Arrays.asList("tl", "tls"));
    }

    @Override
    public List<String> getMimeTypes() {
        return Collections.unmodifiableList(Arrays.asList("text/triton", "application/triton"));
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList("triton");
    }

    @Override
    public String getLanguageName() {
        return "triton";
    }

    @Override
    public String getLanguageVersion() {
        return Compiler.class.getPackage().getImplementationVersion();
    }

    @Override
    public Object getParameter(final String key) {
        if (ScriptEngine.ENGINE.equals(key)) {
            return getEngineName();
        } else if (ScriptEngine.ENGINE_VERSION.equals(key)) {
            return getEngineVersion();
        } else if (ScriptEngine.LANGUAGE.equals(key)) {
            return getLanguageName();
        } else if (ScriptEngine.LANGUAGE_VERSION.equals(key)) {
            return getLanguageVersion();
        } else if (ScriptEngine.NAME.equals(key)) {
            return getEngineName();
        }

        return null;
    }

    @Override
    public String getMethodCallSyntax(final String obj, final String m, final String... args) {
        final StringBuilder builder = new StringBuilder(String.format("%s.%s(", obj, m));

        for (int i = 0; i < args.length; i++) {
            builder.append(args[i]);

            if (i + 1 < args.length)
                builder.append(",");
        }

        return builder.append(")").toString();
    }

    @Override
    public String getOutputStatement(final String toDisplay) {
        return String.format("println(%s)", toDisplay);
    }

    @Override
    public String getProgram(final String... statements) {
        final StringBuilder builder = new StringBuilder();

        for (final String statement : statements) {
            builder.append(statement).append(System.lineSeparator());
        }

        return builder.toString();
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new TritonScriptEngine(this);
    }
}
