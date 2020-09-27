package org.bw.tl;

import lombok.Getter;
import org.bw.tl.antlr.ast.Node;

public enum ErrorType {

    SYNTAX_ERROR("Syntax Error"),
    SYMBOL_ERROR("Symbol Error"),
    GENERAL_ERROR("Error"),
    INTERNAL_ERROR("Internal Error");

    private @Getter final String name;

    ErrorType(final String name) {
        this.name = name;
    }

    public Error newError(final String message, final Node node) {
        return new Error(message, node.getFile(), node,this, node.getLineNumber());
    }

    public Error newError(final String message, final String file, final int lineNumber) {
        return new Error(message, file, null, this, lineNumber);
    }
}
