package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Constraint extends Node {

    private final Block constraint;
    private final Block constraintViolation;

    @Override
    public void accept(final ASTVisitor visitor) {

    }
}
