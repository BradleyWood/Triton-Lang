package org.bw.tl.antlr.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class Module {

    private final @Getter QualifiedName modulePackage;
    private final @Getter List<Field> fields;

    public Module(final QualifiedName modulePackage, final Field[] fields) {
        this(modulePackage, new ArrayList<>(Arrays.asList(fields)));
    }
}
