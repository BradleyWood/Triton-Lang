package org.bw.tl.antlr.ast;

public abstract class Expression extends Node {

    private boolean pop;

    /**
     *
     * @param pop True if the result of the expression is not used
     */
    public void setPop(final boolean pop) {
        this.pop = pop;
    }

    /**
     * If the result of an expression is not used (such as a call)
     * the result needs to be popped from the stack
     *
     * @return Whether the expression needs to be popped off the stack
     */
    public boolean shouldPop() {
        return pop;
    }
}
