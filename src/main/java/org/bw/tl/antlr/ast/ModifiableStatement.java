package org.bw.tl.antlr.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract @Data class ModifiableStatement extends Node {

    private final List<Modifier> modifiers;
    private final List<Annotation> annotations;

    public ModifiableStatement(@NotNull final Modifier[] modifiers, @NotNull final Annotation[] annotations) {
        this(new ArrayList<>(Arrays.asList(modifiers)), new ArrayList<>(Arrays.asList(annotations)));
    }

    public void addModifiers(@NotNull final Modifier... modifiers) {
        this.modifiers.addAll(Arrays.asList(modifiers));
    }

    public boolean hasModifier(final Modifier modifier) {
        return modifiers.contains(modifier);
    }

    public void addAnnotations(@NotNull final Annotation... annotations) {
        this.annotations.addAll(Arrays.asList(annotations));
    }

    public boolean hasAnnotation(@NotNull final QualifiedName name) {
        return this.annotations.stream().anyMatch(a -> name.equals(a.getName()));
    }
}
