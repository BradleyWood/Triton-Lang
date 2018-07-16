package org.bw.tl.verify;

import org.bw.tl.antlr.ast.Node;

public interface Verifiable <T extends Node> {

    boolean isValid(T node);
}
