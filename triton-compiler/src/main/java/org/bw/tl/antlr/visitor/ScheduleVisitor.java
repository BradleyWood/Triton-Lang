package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.ScheduleBlock;
import org.bw.tl.antlr.ast.Task;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(staticName = "of")
public class ScheduleVisitor extends GrammarBaseVisitor<ScheduleBlock> {

    private final @Getter String sourceFile;

    @Override
    public ScheduleBlock visitSchedule(final GrammarParser.ScheduleContext ctx) {
        final List<Task> tasks = new LinkedList<>();
        final TaskVisitor visitor = TaskVisitor.of(sourceFile);
        final ScheduleBlock schedule = new ScheduleBlock(tasks);

        for (final GrammarParser.TaskContext taskContext : ctx.task()) {
            final Task task = taskContext.accept(visitor);

            task.setParent(schedule);
            task.setFile(sourceFile);
            task.setText(taskContext.getText());
            task.setLineNumber(taskContext.start.getLine());
            tasks.add(task);
        }

        return new ScheduleBlock(tasks);
    }
}
