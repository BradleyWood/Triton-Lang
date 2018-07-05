package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.bw.tl.compiler.types.*;

@EqualsAndHashCode(callSuper = true)
public @Data class Literal<T> extends Expression {

    private final T value;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitLiteral(this);
    }

    @Override
    public String getTypeDesc() {
        if (value instanceof String) {
            return "Ljava/lang/String;";
        } else if (value instanceof Double) {
            return DoubleType.INSTANCE.getDesc();
        } else if (value instanceof Float) {
            return FloatType.INSTANCE.getDesc();
        } else if (value instanceof Integer) {
            int v = (Integer) value;
            if (v <= Byte.MAX_VALUE && v > Byte.MIN_VALUE) {
                return ByteType.INSTANCE.getDesc();
            } else if (v <= Short.MAX_VALUE && v >= Short.MIN_VALUE) {
                return ShortType.INSTANCE.getDesc();
            }
            return IntType.INSTANCE.getDesc();
        } else if (value instanceof Boolean) {
            return BoolType.INSTANCE.getDesc();
        }
        return null;
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        return resolver.resolveLiteral(this);
    }
}
