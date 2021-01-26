package org.bw.tl.compiler.resolve;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.util.TypeUtilities;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
public @Data class BlockResolver extends ASTVisitorBase {

    private final ExpressionResolver resolver;
    private List<Expression> retExpressions;
    private Expression lastStmt = null;

    public Type getType(final Block block) {
        retExpressions = new ArrayList<>();

        final Node last = block.getStatements().get(block.getStatements().size() - 1);

        if (last instanceof Expression) {
            lastStmt = (Expression) last;
            block.getStatements().remove(lastStmt);
            final Return ret = new Return(lastStmt);
            block.getStatements().add(ret);
            ret.setParent(block);
        }

        block.accept(this);

        if (retExpressions.isEmpty())
            return Type.VOID_TYPE;

        if (retExpressions.size() == 1) {
            return retExpressions.get(0).resolveType(resolver);
        }

        Type[] expressions = retExpressions.stream().map(m -> m.resolveType(resolver)).toArray(Type[]::new);
        Type retType = expressions[0];

        for (int i = 1; i < expressions.length; i++) {
            retType = findCommonType(retType, expressions[i]);
        }

        return retType;
    }

    private Type findCommonType(Type a, Type b) {
        if (b == null) {
            return null;
        } else if (Objects.equals(a, b)) {
            return a;
        } else if (TypeUtilities.isAssignableFrom(a, b)) {
            return b;
        } else if (TypeUtilities.isAssignableFrom(b, a)) {
            return a;
        } else if (TypeUtilities.isAssignableWithImplicitCast(a, b)) {
            return b;
        } else if (TypeUtilities.isAssignableWithImplicitCast(b, a)) {
            return a;
        } else {
            // todo; find lowest common parent
            return Type.getType(Object.class);
        }
    }

    @Override
    public void visitIf(final IfStatement ifStatement) {
        if (lastStmt.equals(ifStatement)) {
            Type type = ifStatement.resolveType(resolver);

            if (type != null) {
                retExpressions.add(ifStatement);
                return;
            }
        }

        ifStatement.getBody().accept(this);

        if (ifStatement.getElseBody() != null) {
            ifStatement.getElseBody().accept(this);
        }
    }

    @Override
    public void visitReturn(final Return returnStmt) {
        retExpressions.add(returnStmt.getExpression());
    }

    @Override
    public void visitWhile(final WhileLoop whileLoop) {
        whileLoop.getBody().accept(this);
    }

    @Override
    public void visitFor(final ForLoop forLoop) {
        forLoop.getBody().accept(this);
    }
}
