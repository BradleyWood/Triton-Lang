package org.triton.compiler;

import org.bw.tl.antlr.ast.Clazz;
import org.bw.tl.antlr.ast.QualifiedName;
import org.bw.tl.compiler.Scope;
import org.bw.tl.compiler.resolve.ExpressionResolverImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.List;

public class ScriptResolver extends ExpressionResolverImpl {

    public ScriptResolver(final Clazz clazz, final @NotNull List<Clazz> classpath, final Scope scope) {
        super(clazz, classpath, scope);
    }

    @Override
    public @Nullable Type resolveName(@NotNull final QualifiedName name) {
        final Type type = super.resolveName(name);

        if (type != null)
            return type;

        return Type.getType(Object.class);
    }
}
