package org.bw.tl.compiler;

import lombok.Data;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.bw.tl.compiler.resolve.ExpressionResolverImpl;
import org.bw.tl.compiler.resolve.SymbolResolver;
import org.objectweb.asm.Type;

import java.util.LinkedList;
import java.util.List;

public @Data class MethodCtx {

    private final List<Error> errors = new LinkedList<>();
    private final Scope scope = new Scope();
    private ExpressionResolver resolver;
    private final List<Module> classPath;
    private final Function function;
    private final Module module;
    private final File file;

    public ExpressionResolver getResolver() {
        if (resolver == null) {
            SymbolResolver symbolResolver = new SymbolResolver(classPath, module);
            resolver = new ExpressionResolverImpl(symbolResolver, module, file, scope);
        }
        return resolver;
    }

    public Type resolveField(final QualifiedName name) {
        return resolver.resolveName(name);
    }

    public Type resolveFunction(final Call call) {
        return resolver.resolveCall(call);
    }

    public boolean isInitializer() {
        return getMethodName().equals("<clinit>");
    }

    public boolean isMain() {
        return getMethodName().equals("main") && function.getType().toString().equals("void") &&
                function.getParameterTypes().length == 1 &&
                function.getParameterTypes()[0].getDesc().equals(Type.getType(String[].class).getDescriptor());
    }

    public boolean isStatic() {
        return true;
    }

    public String getMethodName() {
        return function.getName();
    }

    public String getInternalClassName() {
        return module.getModulePackage().toString();
    }
}
