package org.triton;

import javax.script.*;
import java.io.Reader;

public class TritonScriptEngine extends AbstractScriptEngine {

    private final ScriptEngineFactory factory;

    public TritonScriptEngine(final ScriptEngineFactory factory) {
        this.factory = factory;
    }

    @Override
    public Object eval(String script, ScriptContext context) {
        return null;
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return null;
    }

    @Override
    public Bindings createBindings() {
        return null;
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }
}
