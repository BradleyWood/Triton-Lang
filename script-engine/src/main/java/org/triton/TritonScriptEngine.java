package org.triton;

import org.triton.iterpreter.TritonInterpreter;

import javax.script.*;
import java.io.IOException;
import java.io.Reader;

public class TritonScriptEngine extends AbstractScriptEngine {

    private final TritonInterpreter interpreter = new TritonInterpreter();
    private final ScriptEngineFactory factory;

    public TritonScriptEngine(final ScriptEngineFactory factory) {
        this.factory = factory;
    }

    @Override
    public Object eval(final String script, final ScriptContext context) throws ScriptException {
        interpreter.setCtx(context);
        return interpreter.eval(script);
    }

    @Override
    public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
        interpreter.setCtx(context);
        try {
            return interpreter.eval(reader);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }
}
