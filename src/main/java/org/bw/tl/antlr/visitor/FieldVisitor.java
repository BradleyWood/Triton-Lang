package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.AllArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.Field;
import org.bw.tl.antlr.ast.QualifiedName;

@AllArgsConstructor(staticName = "of")
public class FieldVisitor extends GrammarBaseVisitor<Field> {

    private final @Getter String sourceFile;

    @Override
    public Field visitVarDef(final GrammarParser.VarDefContext ctx) {
        final String name = ctx.IDENTIFIER().getText();
        final QualifiedName type = ctx.type() != null ? QualifiedName.of(ctx.type().getText()) : null;
        Expression initialValue = null;

        if (ctx.expression() != null)
            initialValue = ctx.expression().accept(ExpressionVisitor.of(sourceFile));

        final Field field = new Field(name, type, initialValue);

        field.setConstant(ctx.VAL() != null);

        if (ctx.modifierList() != null && ctx.modifierList().modifier() != null) {
            for (final GrammarParser.ModifierContext modCtx : ctx.modifierList().modifier()) {
                field.addModifiers(modCtx.accept(new ModifierVisitor()));
            }
        }

        if (initialValue != null)
            initialValue.setParent(field);

        field.setText(ctx.getText());
        field.setLineNumber(ctx.start.getLine());
        field.setFile(sourceFile);

        return field;
    }
}
