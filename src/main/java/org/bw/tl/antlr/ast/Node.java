package org.bw.tl.antlr.ast;

public abstract class Node {

    private int lineNumber = -1;
    private String text;

    public abstract void accept(final ASTVisitor visitor);

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
