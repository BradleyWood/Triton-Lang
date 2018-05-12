package org.bw.tl.antlr.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public @Data class Annotation extends Node {

    private final QualifiedName name;
    private final List<SimpleImmutableEntry<QualifiedName, Expression>> pairs;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitAnnotation(this);
    }

    public Annotation(final QualifiedName name, final QualifiedName[] keys, final Expression[] values) {
        this(name, IntStream.range(0, keys.length)
                .mapToObj(i -> new SimpleImmutableEntry<>(keys[i], values[i])).collect(Collectors.toList()));
    }
}
