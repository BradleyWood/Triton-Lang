package org.bw.tl.compiler;

import lombok.Data;
import org.bw.tl.Error;
import org.bw.tl.ErrorType;
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
    private SymbolResolver symbolResolver;
    private final List<Module> classPath;
    private final Function function;
    private final Module module;
    private final File file;

    public ExpressionResolver getResolver() {
        if (resolver == null) {
            resolver = new ExpressionResolverImpl(getSymbolResolver(), module, file, scope);
        }
        return resolver;
    }

    public SymbolResolver getSymbolResolver() {
        if (symbolResolver == null) {
            symbolResolver = new SymbolResolver(classPath, module);
        }
        return symbolResolver;
    }

    public Type getReturnType() {
        return getSymbolResolver().resolveType(function.getType());
    }

    public Type resolveField(final QualifiedName name) {
        return resolver.resolveName(name);
    }

    public Type resolveFunction(final Call call) {
        return resolver.resolveCall(call);
    }

    public Type resolveType(final QualifiedName name) {
        return symbolResolver.resolveType(name);
    }

    public boolean isInitializer() {
        return getMethodName().equals("<clinit>");
    }

    public boolean isSynthetic() {
        return file == null;
    }

    public void beginScope() {
        scope.beginScope();
    }

    public void endScope() {
        scope.endScope();
    }

    public void reportError(final Node node) {
        reportError("", node);
    }

    public void reportError(final String message, final Node node) {
        reportError(ErrorType.GENERAL_ERROR, message, node);
    }

    public void reportError(final ErrorType errorType, final String message, final Node node) {
        errors.add(errorType.newError(message, node));
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
