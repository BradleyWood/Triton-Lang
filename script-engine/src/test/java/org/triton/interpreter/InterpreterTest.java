package org.triton.interpreter;

import org.junit.Assert;
import org.junit.Test;
import org.triton.iterpreter.TritonInterpreter;

import javax.script.ScriptException;

public class InterpreterTest {

    private final TritonInterpreter interpreter = new TritonInterpreter();

    @Test
    public void testEvalReturnValue() throws ScriptException {
        Assert.assertEquals(10, interpreter.eval("5 + 5"));

        Assert.assertEquals("Hello, World", interpreter.eval("\"Hello, World\""));

        Assert.assertEquals(1000, interpreter.eval("var a = 1000; a"));

        Assert.assertEquals(11, interpreter.eval("5 + 5; 11"));

        Assert.assertEquals(interpreter.eval(""), Void.TYPE);
    }
}
