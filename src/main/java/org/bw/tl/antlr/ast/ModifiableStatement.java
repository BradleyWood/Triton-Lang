package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
public abstract @Data class ModifiableStatement extends Node {

    private final List<Modifier> modifiers = new ArrayList<>();
    private final List<Annotation> annotations = new ArrayList<>();

    public void addModifiers(@NotNull final Modifier... modifiers) {
        this.modifiers.addAll(Arrays.asList(modifiers));
    }

    public boolean hasModifier(final Modifier modifier) {
        return modifiers.contains(modifier);
    }

    public void addAnnotations(@NotNull final Annotation... annotations) {
        this.annotations.addAll(Arrays.asList(annotations));
    }

    public boolean hasAnnotation(final QualifiedName name) {
        return this.annotations.stream().anyMatch(a -> a.getName().equals(name));
    }
}
