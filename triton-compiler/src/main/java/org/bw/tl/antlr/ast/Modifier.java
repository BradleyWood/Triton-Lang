package org.bw.tl.antlr.ast;


import lombok.Getter;
import org.objectweb.asm.Opcodes;

public enum Modifier {

    PUBLIC("public", Opcodes.ACC_PUBLIC),
    PRIVATE("private", Opcodes.ACC_PRIVATE),
    PROTECTED("protected", Opcodes.ACC_PROTECTED),
    STATIC("static", Opcodes.ACC_STATIC),
    FINAL("final", Opcodes.ACC_FINAL),
    SYNTHETIC("synthetic", Opcodes.ACC_SYNTHETIC);

    @Getter private final String name;
    @Getter private final int value;

    Modifier(final String name, final int value) {
        this.name = name;
        this.value = value;
    }
}
