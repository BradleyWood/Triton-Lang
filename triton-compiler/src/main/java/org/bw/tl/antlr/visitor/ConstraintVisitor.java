package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Block;
import org.bw.tl.antlr.ast.Constraint;

@RequiredArgsConstructor(staticName = "of")
public class ConstraintVisitor extends GrammarBaseVisitor<Constraint> {

    private final @Getter String sourceFile;

    @Override
    public Constraint visitConstraint(final GrammarParser.ConstraintContext ctx) {
        final BlockVisitor blockVisitor = BlockVisitor.of(sourceFile);
        final Block condition = ctx.condition.accept(blockVisitor);
        Block onViolation = null;

        if (ctx.violation != null) {
            onViolation = ctx.violation.accept(blockVisitor);
            onViolation.setFile(sourceFile);
            onViolation.setText(ctx.violation.getText());
            onViolation.setLineNumber(ctx.violation.start.getLine());
        }

        final Constraint constraint = new Constraint(condition, onViolation);

        if (onViolation != null) {
            onViolation.setParent(condition);
        }

        condition.setFile(sourceFile);
        condition.setParent(constraint);
        condition.setText(ctx.condition.getText());
        condition.setLineNumber(ctx.condition.start.getLine());

        return constraint;
    }
}
