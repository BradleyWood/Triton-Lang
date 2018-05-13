package org.bw.tl.antlr.visitor;

import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Modifier;

public class ModifierVisitor extends GrammarBaseVisitor<Modifier> {

    @Override
    public Modifier visitModifier(final GrammarParser.ModifierContext ctx) {
        Modifier modifier = null;

        if (ctx.visibilityModifier() != null) {
            modifier = Modifier.valueOf(ctx.visibilityModifier().getText().toUpperCase());
        }

        if (modifier == null)
            throw new IllegalStateException("Unimplemented modifier type");

        return modifier;
    }
}
