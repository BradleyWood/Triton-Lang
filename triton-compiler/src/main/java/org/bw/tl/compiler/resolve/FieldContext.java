package org.bw.tl.compiler.resolve;

import lombok.Getter;
import org.objectweb.asm.Type;

public class FieldContext extends SymbolContext {

    private @Getter final boolean isLocal;

    public FieldContext(final String name, final String owner, final Type typeDescriptor, final int accessModifiers, final boolean isLocal) {
        super(name, owner, typeDescriptor, accessModifiers);
        this.isLocal = isLocal;
    }
}
