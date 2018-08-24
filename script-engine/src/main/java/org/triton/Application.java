package org.triton;

import org.triton.iterpreter.TritonInterpreter;

import javax.script.ScriptException;
import java.io.IOException;

public class Application {

    public static void main(String[] args) {
        if (args.length <= 0) {
            System.err.println("Missing path argument");
        } else {
            try {
                final TritonInterpreter interpreter = new TritonInterpreter();
                interpreter.execFile(args[0]);
            } catch (IOException e) {
                System.err.println(String.format("Cannot open file: \"%s\" for reading", args[0]));
            } catch (ScriptException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
