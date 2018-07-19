package org.bw.tl.compiler.resolve;

import lombok.Data;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;

public @Data class SymbolContext {

    private final String name;
    private final String owner;
    private final Type typeDescriptor;
    private final int accessModifiers;

    public boolean isPublic() {
        return Modifier.isPublic(accessModifiers);
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(accessModifiers);
    }

    public boolean isProtected() {
        return Modifier.isProtected(accessModifiers);
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(accessModifiers);
    }

    public boolean isTransient() {
        return Modifier.isTransient(accessModifiers);
    }

    public boolean isFinal() {
        return Modifier.isFinal(accessModifiers);
    }

    public boolean isInterface() {
        return Modifier.isInterface(accessModifiers);
    }

    public boolean isNative() {
        return Modifier.isNative(accessModifiers);
    }

    public boolean isStatic() {
        return Modifier.isStatic(accessModifiers);
    }

    public boolean isStrict() {
        return Modifier.isStrict(accessModifiers);
    }

    public boolean isSynchronized() {
        return Modifier.isSynchronized(accessModifiers);
    }

    public boolean isVolatile() {
        return Modifier.isVolatile(accessModifiers);
    }
}
