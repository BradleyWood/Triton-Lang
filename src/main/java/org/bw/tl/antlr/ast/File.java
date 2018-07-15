package org.bw.tl.antlr.ast;

import lombok.Data;
import org.objectweb.asm.Type;

import java.util.List;

import static org.bw.tl.util.FileUtilities.getType;

public @Data class File {

    private final QualifiedName packageName;
    private final List<QualifiedName> imports;
    private final List<Field> fields;
    private final List<Function> functions;
    private final String sourceFile;


    public Type resolveFunction(final Function function) {
        final QualifiedName[] parameterTypes = function.getParameterTypes();
        final Type retType = getType(this, function.getType());
        final Type[] paramTypes = new Type[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            paramTypes[i] = getType(this, parameterTypes[i]);
            if (paramTypes[i] == null)
                return null;
        }

        if (retType == null)
            return null;

        return Type.getMethodType(retType, paramTypes);
    }
}
