package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleBlock extends Node {

    private final List<Task> tasks;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitScheduleBlock(this);
    }
}
