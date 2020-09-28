package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@EqualsAndHashCode(callSuper = true)
public class Task extends Node {

    private final List<String> taskParams = new LinkedList<>();
    private final int period;
    private final TimeUnit timeUnit;
    private final Block body;
    private final List<Constraint> constraints;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitTask(this);
    }
}
