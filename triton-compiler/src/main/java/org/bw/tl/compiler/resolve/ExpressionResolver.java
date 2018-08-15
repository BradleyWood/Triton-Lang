package org.bw.tl.compiler.resolve;

import org.bw.tl.antlr.ast.*;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public interface ExpressionResolver {

    Type resolveBinaryOp(BinaryOp bop);

    Type resolveUnaryOp(UnaryOp op);

    Type resolveCall(Call call);

    Type resolveLiteral(Literal literal);

    Type resolveName(QualifiedName name);

    Type resolveConstructor(New newStmt);

    Type resolveTypeCast(TypeCast typeCast);

    Type resolveTypeName(TypeName typeName);

    SymbolContext resolveCallCtx(Call call);

    SymbolContext resolveConstructorContext(New newStmt);

    FieldContext[] resolveFieldContext(QualifiedName name);

    FieldContext resolveFieldContext(Expression preceding, String name);

    SymbolContext resolveFunction(@NotNull final Type owner, @NotNull final String name, @NotNull final Type... parameterTypes);

    SymbolContext resolveFunction(final @NotNull Class<?> clazz, @NotNull final String name, @NotNull final Type... parameterTypes);

    Type resolveType(@NotNull final QualifiedName name);

    SymbolContext resolveConstructor(@NotNull final QualifiedName owner, @NotNull final Type... parameterTypes);

    SymbolContext resolveConstructor(@NotNull final Type owner, @NotNull final Type... parameterTypes);

    FieldContext resolveField(@NotNull final Type owner, @NotNull final String name);

    FieldContext resolveField(@NotNull final Class<?> clazz, @NotNull final String name);

    SymbolContext resolveFunctionContext(@NotNull final String name, @NotNull final Type... parameterTypes);

    Function resolveFunction(@NotNull final String name, @NotNull final Type... parameterTypes);

    Type resolveType(@NotNull final Clazz clazz, final TypeName name);

    Type resolveFunction(@NotNull final Clazz clazz, @NotNull final Function function);

    SymbolContext resolveCallFromStaticImports(@NotNull final String name, @NotNull final Type... parameterTypes);

    FieldContext resolveFieldFromStaticImports(@NotNull final String name);

    Type resolveFunctionType(@NotNull final String name, @NotNull final Type... parameterTypes);

    Field resolveField(@NotNull final Clazz clazz, @NotNull final String name);

    Type resolveFieldType(@NotNull final Clazz clazz, @NotNull final String name);

}
