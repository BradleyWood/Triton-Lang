package org.triton.compiler;

import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.MethodCtx;
import org.bw.tl.compiler.MethodImpl;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

public class ScriptMethodImpl extends MethodImpl {

    ScriptMethodImpl(@NotNull final MethodVisitor mv, final @NotNull MethodCtx ctx) {
        super(mv, ctx, new ScriptExpressionImpl(mv, ctx));
    }

    @Override
    public void visitFunction(final Function function) {
        if (isEvalMethod()) {
            getCtx().beginScope();

            defineParameters(function);

            Node ret = new Return(new Literal<>(null));

            if (function.getBody() instanceof Block) {
                final Block block = (Block) function.getBody();
                final List<Node> statements = block.getStatements();

                if (!statements.isEmpty()) {
                    final Node last = statements.get(statements.size() - 1);

                    if (last instanceof Expression) {
                        final Type type = ((Expression) last).resolveType(getCtx().getResolver());

                        if (!Type.VOID_TYPE.equals(type)) {
                            statements.remove(statements.get(statements.size() - 1));
                            ret = new Return((Expression) last);
                        }
                    }
                }
            }

            function.getBody().accept(this);

            ret.accept(this);

            getCtx().endScope();
        } else {
            super.visitFunction(function);
        }
    }

    @Override
    public void visitField(final Field field) {
        if (field.getInitialValue() == null) {
            if (field.getType() == null) {
                getCtx().reportError("Cannot infer type", field);
                return;
            }

            final Type fieldType = field.getType().resolveType(getCtx().getResolver());

            if (fieldType == null) {
                getCtx().reportError("cannot resolve type", field.getType());
                return;
            }

            if (!getCtx().getScope().putVar(field.getName(), fieldType, field.getAccessModifiers()))
                getCtx().reportError("Field: " + field.getName() + " has already been defined", field);

        } else {
            super.visitField(field);
        }
    }

    private boolean isEvalMethod() {
        return "eval".equals(getCtx().getMethodName()) && !getCtx().isStatic();
    }
}
