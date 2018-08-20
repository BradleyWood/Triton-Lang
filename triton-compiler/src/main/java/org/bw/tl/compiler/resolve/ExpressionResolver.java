package org.bw.tl.compiler.resolve;

import org.bw.tl.antlr.ast.*;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public interface ExpressionResolver {

    Type resolveCall(Call call);

    Type resolveUnaryOp(UnaryOp op);

    Type resolveBinaryOp(BinaryOp bop);

    Type resolveLiteral(Literal literal);

    Type resolveName(QualifiedName name);

    Type resolveConstructor(New newStmt);

    Type resolveTypeCast(TypeCast typeCast);

    Type resolveTypeName(TypeName typeName);

    Type resolveType(QualifiedName name);

    SymbolContext resolveCallCtx(Call call);

    SymbolContext resolveConstructorCtx(New newStmt);

    FieldContext[] resolveFieldCtx(QualifiedName name);

    FieldContext resolveFieldCtx(Expression preceding, String name);

    Type resolveFunctionCtx(@NotNull final Clazz clazz, @NotNull final Function function);

}
