package org.bw.tl;

import lombok.Data;
import lombok.Getter;
import org.bw.tl.antlr.ast.Node;

import java.io.PrintStream;

public @Data class Error {

    private final String message;
    private final String file;
    private final TYPE errorType;
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
        return errorType.getName() + ": " + message + System.lineSeparator() +
                " at " + file + " on line " + lineNumber;
    }

    public enum TYPE {

        SYNTAX_ERROR("Syntax Error"),
        SYMBOL_ERROR("Symbol Error"),
        INTERNAL_ERROR("Internal Error");

        private @Getter String name;

        TYPE(final String name) {
            this.name = name;
        }

        public Error newError(final String message, final Node node) {
            return new Error(message, node.getFile(), this, node.getLineNumber());
        }

        public Error newError(final String message, final String file, final int lineNumber) {
            return new Error(message, file, this, lineNumber);
        }
    }
}
