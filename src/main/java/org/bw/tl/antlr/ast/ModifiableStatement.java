package org.bw.tl.antlr.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract @Data class ModifiableStatement extends Node {

    private final List<Modifier> modifiers;

    public ModifiableStatement(final Modifier... modifiers) {
        this(new ArrayList<>(Arrays.asList(modifiers)));
    }

    public void addModifiers(final Modifier... modifiers) {
        this.modifiers.addAll(Arrays.asList(modifiers));
    }

    public boolean hasModifier(final Modifier modifier) {
        return modifiers.contains(modifier);
    }
}
