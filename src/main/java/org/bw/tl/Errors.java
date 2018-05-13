package org.bw.tl;

import java.io.PrintStream;
import java.util.LinkedList;

public class Errors {

    private static final LinkedList<Error> errors = new LinkedList<>();

    public static void addError(final Error error) {
        errors.add(error);
    }

    public static void printErrors() {
        printErrors(System.err);
    }

    public static void printErrors(final PrintStream printStream) {
        for (final Error error : errors) {
            error.print(printStream);
            printStream.println();
        }
    }

    public static int getErrorCount() {
        return errors.size();
    }
}
