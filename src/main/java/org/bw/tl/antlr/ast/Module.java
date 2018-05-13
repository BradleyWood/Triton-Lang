package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public @Data class Module {

    private final QualifiedName modulePackage;
    private final List<Field> fields;

    public Module(final QualifiedName modulePackage, final Field[] fields) {
        this(modulePackage, new ArrayList<>(Arrays.asList(fields)));
    }
}
