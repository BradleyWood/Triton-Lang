package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Block;
import org.bw.tl.antlr.ast.Constraint;
import org.bw.tl.antlr.ast.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor(staticName = "of")
public class TaskVisitor extends GrammarBaseVisitor<Task> {

    private final @Getter String sourceFile;

    @Override
    public Task visitTask(final GrammarParser.TaskContext ctx) {
        final BlockVisitor blockVisitor = BlockVisitor.of(sourceFile);
        final Block body = ctx.block().accept(blockVisitor);
        final int period = Integer.parseInt(ctx.taskParams().period.getText());
        final List<Constraint> constraints = new LinkedList<>();
        final Task task = new Task(period, TimeUnit.MILLISECONDS, body, constraints);

        final List<GrammarParser.ConstraintContext> constraintCtxs = ctx.constraint();
        final ConstraintVisitor cv = ConstraintVisitor.of(sourceFile);

        for (final GrammarParser.ConstraintContext constraintCtx : constraintCtxs) {
            final Constraint constraint = constraintCtx.accept(cv);

            constraint.setParent(task);
            constraint.setFile(sourceFile);
            constraint.setText(constraintCtx.getText());
            constraint.setLineNumber(constraintCtx.start.getLine());
            constraints.add(constraint);
        }

        body.setParent(task);
        body.setFile(sourceFile);
        body.setLineNumber(ctx.block().start.getLine());
        body.setText(ctx.block().getText());

        return task;
    }
}
