package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.*;

@RequiredArgsConstructor(staticName = "of")
public class ForLoopVisitor extends GrammarBaseVisitor<Node> {

    private final @Getter String sourceFile;

    @Override
    public Node visitForStatement(final GrammarParser.ForStatementContext ctx) {
        final Node body = ctx.statement().accept(StatementVisitor.of(sourceFile));

        final GrammarParser.ForControlContext fctx = ctx.forControl();

        if (ctx.forControl().COLON() != null) { // for each
            final TypeName type = fctx.type() != null ? TypeName.of(fctx.type().getText()) : null;
            final Expression iterableExpression = fctx.expression(0).accept(ExpressionVisitor.of(sourceFile));
            final Field field = new Field(fctx.IDENTIFIER().getText(), type, null);

            if (fctx.modifierList() != null && fctx.modifierList().modifier() != null) {
                for (final GrammarParser.ModifierContext modCtx : fctx.modifierList().modifier()) {
                    field.addModifiers(modCtx.accept(new ModifierVisitor()));
                }
            }

            if (fctx.VAL() != null)
                field.addModifiers(Modifier.FINAL);

            return new ForEachLoop(field, iterableExpression, body);
        } else { // for i
            // todo;
        }

        return null;
    }
}
