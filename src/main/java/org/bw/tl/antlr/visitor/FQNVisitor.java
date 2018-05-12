package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.QualifiedName;

@RequiredArgsConstructor(staticName = "of")
public class FQNVisitor extends GrammarBaseVisitor<QualifiedName> {

    private final @Getter String sourceFile;

    @Override
    public QualifiedName visitFqn(final GrammarParser.FqnContext ctx) {
        final QualifiedName fqn = new QualifiedName(ctx.IDENTIFIER().stream().map(ParseTree::getText).toArray(String[]::new));

        fqn.setLineNumber(ctx.start.getLine());
        fqn.setText(ctx.getText());
        fqn.setFile(sourceFile);

        return fqn;
    }
}
