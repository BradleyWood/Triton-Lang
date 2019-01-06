package org.triton;

import org.triton.iterpreter.TritonInterpreter;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.Scanner;

public class Application {

    public static void main(String[] args) {
        final TritonInterpreter interpreter = new TritonInterpreter();

        if (args.length <= 0) {
            final Scanner sc = new Scanner(System.in);

            while (true) {
                final String line = sc.nextLine();

                try {
                    interpreter.exec(line);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }

        } else {
            try {
                interpreter.execFile(args[0]);
            } catch (IOException e) {
                System.err.println(String.format("Cannot open file: \"%s\" for reading", args[0]));
            } catch (ScriptException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
