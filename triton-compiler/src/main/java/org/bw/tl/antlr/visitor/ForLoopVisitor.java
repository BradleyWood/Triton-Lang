package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(staticName = "of")
public class ForLoopVisitor extends GrammarBaseVisitor<Node> {

    private final @Getter String sourceFile;

    @Override
    public Node visitForStatement(final GrammarParser.ForStatementContext ctx) {
        final Node body = ctx.statement().accept(StatementVisitor.of(sourceFile));

        final GrammarParser.ForControlContext fctx = ctx.forControl();

        if (fctx != null && ctx.forControl().COLON() != null) { // for each
            final TypeName type = fctx.type() != null ? fctx.type().accept(TypeVisitor.of(sourceFile)) : null;
            final Expression iterableExpression = fctx.expression(0).accept(ExpressionVisitor.of(sourceFile));
            final Field field = new Field(fctx.IDENTIFIER().getText(), type, null);

            if (fctx.modifierList() != null && fctx.modifierList().modifier() != null) {
                for (final GrammarParser.ModifierContext modCtx : fctx.modifierList().modifier()) {
                    field.addModifiers(modCtx.accept(new ModifierVisitor()));
                }
            }

            final ForEachLoop loop = new ForEachLoop(field, iterableExpression, body);

            if (fctx.VAL() != null)
                field.addModifiers(Modifier.FINAL);

            field.setParent(loop);
            iterableExpression.setParent(loop);
            body.setParent(loop);

            return loop;
        } else if (fctx != null) { // for i
            Node init = null;

            if (fctx.init != null) {
                init = fctx.init.accept(ExpressionVisitor.of(sourceFile));
            } else if (fctx.varDef() != null) {
                init = fctx.varDef().accept(FieldVisitor.of(sourceFile));
            }

            final Expression condition = fctx.condition != null ? fctx.condition.accept(ExpressionVisitor.of(sourceFile)) : null;
            final List<Expression> update = new LinkedList<>();

            final ForLoop loop = new ForLoop(init, condition, update, body);

            if (fctx.expressionList() != null)
                fctx.expressionList().expression().forEach(e -> update.add(e.accept(ExpressionVisitor.of(sourceFile))));

            if (init != null)
                init.setParent(loop);

            body.setParent(loop);
            update.forEach(e -> e.setParent(loop));
            update.forEach(e -> e.setPop(true));

            return loop;
        } else { // infinite loop
            final ForLoop loop = new ForLoop(null, null, Collections.emptyList(), body);
            body.setParent(loop);

            return loop;
        }
    }
}
