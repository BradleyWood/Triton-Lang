package org.bw.tl.antlr.visitor;

import org.antlr.v4.runtime.tree.ParseTree;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.QualifiedName;

public class FQNVisitor extends GrammarBaseVisitor<QualifiedName> {

    @Override
    public QualifiedName visitFqn(final GrammarParser.FqnContext ctx) {
        return new QualifiedName(ctx.IDENTIFIER().stream().map(ParseTree::getText).toArray(String[]::new));
    }
}
