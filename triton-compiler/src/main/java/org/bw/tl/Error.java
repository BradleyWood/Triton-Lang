package org.bw.tl;

import lombok.Data;
import org.bw.tl.antlr.ast.Node;

import java.io.PrintStream;

public @Data class Error {

    private final String message;
    private final String file;
    private final Node node;
    private final ErrorType errorType;
    private final int lineNumber;

    /**
     * Prints the error message to the system error stream
     */
    public void print() {
        print(System.err);
    }

    /**
     * Print the error message to specified print stream
     *
     * @param printStream the stream
     */
    public void print(final PrintStream printStream) {
        printStream.println(this);
    }

    @Override
    public String toString() {
        String str = errorType.getName() + ": " + message + System.lineSeparator() +
                "\tat " + file + " on line " + lineNumber;

        if (node != null) {
            str += System.lineSeparator() + "\tat\t\"" + node.getText() + "\"";
        }

        return str;
    }

}
