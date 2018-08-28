package org.bw.tl.antlr.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.util.TypeUtilities;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = false)
public @Data class IfStatement extends Expression {

    private final Expression condition;
    private final Node body;
    private final Node elseBody;

    @Override
    public void accept(final ASTVisitor visitor) {
        visitor.visitIf(this);
    }

    @Override
    public Type resolveType(final ExpressionResolver resolver) {
        final Type bodyType = resolveNode(body, resolver);

        if (elseBody != null && bodyType != null) {
            final Type elseBodyType = resolveNode(elseBody, resolver);

            if (elseBodyType == null) {
                return null;
            } else if (Objects.equals(bodyType, elseBodyType)) {
                return bodyType;
            } else if (TypeUtilities.isAssignableFrom(bodyType, elseBodyType)) {
                return elseBodyType;
            } else if (TypeUtilities.isAssignableFrom(elseBodyType, bodyType)) {
                return bodyType;
            } else if (TypeUtilities.isAssignableWithImplicitCast(bodyType, elseBodyType)) {
                return elseBodyType;
            } else if (TypeUtilities.isAssignableWithImplicitCast(elseBodyType, bodyType)) {
                return bodyType;
            } else {
                return null;
            }
        }

        return bodyType;
    }

    private Type resolveNode(final Node node, final ExpressionResolver resolver) {
        if (node instanceof Block) {
            final Block block = (Block) node;
            final List<Node> stmts = block.getStatements();

            if (!stmts.isEmpty())
                return resolveNode(stmts.get(stmts.size() - 1), resolver);
        } else if (node instanceof Expression) {
            final Expression expr = (Expression) node;
            return expr.resolveType(resolver);
        }

        return null;
    }
}
