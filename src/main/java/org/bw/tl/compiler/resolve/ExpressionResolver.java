package org.bw.tl.compiler.resolve;

import org.bw.tl.antlr.ast.*;
import org.objectweb.asm.Type;

public interface ExpressionResolver {

    Type resolveBinaryOp(BinaryOp bop);

    Type resolveUnaryOp(UnaryOp op);

    Type resolveCall(Call call);

    Type resolveLiteral(Literal literal);

    Type resolveName(QualifiedName name);

}
