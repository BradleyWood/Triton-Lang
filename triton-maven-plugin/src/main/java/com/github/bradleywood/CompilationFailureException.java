package com.github.bradleywood;

import org.apache.maven.plugin.MojoFailureException;

public class CompilationFailureException extends MojoFailureException {

    public CompilationFailureException() {
        super("Compilation failure");
    }

    public CompilationFailureException(final String message) {
        super(message);
    }
}