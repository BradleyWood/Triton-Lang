package org.bw.tl.antlr.visitor;

import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Modifier;

public class ModifierVisitor extends GrammarBaseVisitor<Modifier> {

    @Override
    public Modifier visitModifier(final GrammarParser.ModifierContext ctx) {
        final Modifier modifier = Modifier.valueOf(ctx.getText().toUpperCase());

        if (modifier == null)
            throw new IllegalStateException("Unimplemented modifier type: " + ctx.getText());

        return modifier;
    }
}
