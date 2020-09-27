package org.triton.compiler;

import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.MethodCtx;
import org.bw.tl.compiler.MethodImpl;
import org.bw.tl.compiler.resolve.ExpressionResolverImpl;
import org.bw.tl.compiler.resolve.FieldContext;
import org.bw.tl.compiler.types.Primitive;
import org.bw.tl.compiler.types.TypeHandler;
import org.bw.tl.util.TypeUtilities;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.script.ScriptContext;
import java.util.HashSet;
import java.util.List;

public class ScriptMethodImpl extends MethodImpl {

    private final HashSet<String> varSet = new HashSet<>();

    ScriptMethodImpl(@NotNull final MethodVisitor mv, final @NotNull MethodCtx ctx) {
        super(mv, ctx);
    }

    @Override
    public void visitFunction(final Function function) {
        if (isEvalMethod()) {
            ctx.beginScope();

            defineParameters(function);

            Node lastStmt = function.getBody();

            if (lastStmt instanceof Block) {
                final Block block = (Block) function.getBody();
                final List<Node> statements = block.getStatements();

                if (!statements.isEmpty()) {
                    lastStmt = statements.get(statements.size() - 1);
                }
            }

            if (lastStmt instanceof Expression) {
                ((Expression) lastStmt).setPop(false);
            }

            function.getBody().accept(this);

            if (lastStmt instanceof Expression) {
                final Type lastType = ((Expression) lastStmt).resolveType(ctx.getResolver());

                if (lastType == null) {
                    // error would already have been reported
                    return;
                }

                if (Type.VOID_TYPE.equals(lastType)) {
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;");
                } else {
                    final TypeHandler handler = TypeUtilities.getTypeHandler(lastType);
                    handler.toObject(mv);
                }
            } else {
                mv.visitFieldInsn(GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;");
            }

            mv.visitInsn(ARETURN);

            ctx.endScope();
        } else {
            super.visitFunction(function);
        }
    }

    @Override
    public void visitField(final Field field) {
        if (field.getInitialValue() == null) {
            if (field.getType() == null) {
                ctx.reportError("Cannot infer type", field);
                return;
            }

            final Type fieldType = field.getType().resolveType(ctx.getResolver());

            if (fieldType == null) {
                ctx.reportError("Cannot resolve type", field.getType());
                return;
            }

            if (!ctx.getScope().putVar(field.getName(), fieldType, field.getAccessModifiers()))
                ctx.reportError("Field: " + field.getName() + " has already been defined", field);

        } else {
            super.visitField(field);
        }
    }

    private void storeAttribute(final String name, final Expression expression, final boolean duplicate) {
        final String putDesc = "(Ljava/lang/String;Ljava/lang/Object;I)V";
        final int bindingsIdx = ctx.getScope().findVar(" __ctx__ ").getIndex();

        final Type type = expression.resolveType(ctx.getResolver());
        final TypeHandler handler = TypeUtilities.getTypeHandler(type);

        if (duplicate) {
            expression.accept(this);
            handler.dup(mv);

            mv.visitVarInsn(ALOAD, bindingsIdx);

            if (Type.LONG_TYPE.equals(type) || Type.DOUBLE_TYPE.equals(type)) {
                mv.visitInsn(DUP_X2);
                mv.visitInsn(POP);
            } else {
                mv.visitInsn(SWAP);
            }

            mv.visitLdcInsn(name);

            if (Type.LONG_TYPE.equals(type) || Type.DOUBLE_TYPE.equals(type)) {
                mv.visitInsn(DUP_X2);
                mv.visitInsn(POP);
            } else {
                mv.visitInsn(SWAP);
            }
        } else {
            mv.visitVarInsn(ALOAD, bindingsIdx);
            mv.visitLdcInsn(name);
            expression.accept(this);
        }

        handler.toObject(mv);

        mv.visitIntInsn(BIPUSH, ScriptContext.ENGINE_SCOPE);

        mv.visitMethodInsn(INVOKEINTERFACE, "javax/script/ScriptContext", "setAttribute", putDesc, true);
        varSet.add(name);
    }

    private void loadAttribute(final String attribute) {
        final String containsKey = "(Ljava/lang/String;)I";
        final String getDesc = "(Ljava/lang/String;)Ljava/lang/Object;";
        final int bindingsIdx = ctx.getScope().findVar(" __ctx__ ").getIndex();

        if (!varSet.contains(attribute)) {
            final Label exceptionEnd = new Label();

            mv.visitVarInsn(ALOAD, bindingsIdx);
            mv.visitLdcInsn(attribute);

            mv.visitMethodInsn(INVOKEINTERFACE, "javax/script/ScriptContext", "getAttributesScope", containsKey, true);
            mv.visitJumpInsn(IFGE, exceptionEnd);

            throwException("java/lang/NoSuchFieldException", attribute);
            mv.visitLabel(exceptionEnd);
            varSet.add(attribute);
        }


        mv.visitVarInsn(ALOAD, bindingsIdx);
        mv.visitLdcInsn(attribute);
        mv.visitMethodInsn(INVOKEINTERFACE, "javax/script/ScriptContext", "getAttribute", getDesc, true);
    }

    private void throwException(final String type, final String message) {
        mv.visitTypeInsn(NEW, type);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(String.valueOf(message));
        mv.visitMethodInsn(INVOKESPECIAL, type, "<init>", "(Ljava/lang/String;)V", false);

        mv.visitInsn(ATHROW);
    }

    @Override
    public void visitAssignment(final Assignment assignment) {
        if (assignment.getPrecedingExpr() == null) {
            storeAttribute(assignment.getName(), assignment.getValue(), !assignment.shouldPop());
        } else {
            super.visitAssignment(assignment);
        }
    }

    private void loadField(final FieldContext fieldCtx) {
        final Type type = fieldCtx.getTypeDescriptor();
        final TypeHandler handler = TypeUtilities.getTypeHandler(type);
        TypeHandler boxedHandler = null;

        if (handler.isPrimitive()) {
            final String boxed = Primitive.getPrimitiveByDesc(type.getDescriptor()).getWrappedType();
            boxedHandler = TypeUtilities.getTypeHandler(Type.getType(boxed));
        }

        if (fieldCtx == ExpressionResolverImpl.ARRAY_LENGTH) {
            mv.visitInsn(ARRAYLENGTH);
        } if (isEvalMethod() && fieldCtx.isLocal()) {
            loadAttribute(fieldCtx.getName());

            if (boxedHandler != null) {
                mv.visitTypeInsn(CHECKCAST, boxedHandler.getInternalName());
                handler.cast(mv, boxedHandler);
            } else {
                mv.visitTypeInsn(CHECKCAST, fieldCtx.getTypeDescriptor().getInternalName());
            }
        } else if (fieldCtx.isLocal()) {
            handler.load(mv, ctx.getScope().findVar(fieldCtx.getName()).getIndex());
        } else if (fieldCtx.isStatic()) {
            mv.visitFieldInsn(GETSTATIC, fieldCtx.getOwner(), fieldCtx.getName(), fieldCtx.getTypeDescriptor().getDescriptor());
        } else {
            mv.visitFieldInsn(GETFIELD, fieldCtx.getOwner(), fieldCtx.getName(), fieldCtx.getTypeDescriptor().getDescriptor());
        }
    }

    @Override
    public void visitName(final QualifiedName name) {
        final FieldContext[] ctxList = ctx.getResolver().resolveFieldCtx(name);

        if (ctxList != null) {
            for (final FieldContext fieldCtx : ctxList) {
                loadField(fieldCtx);
            }
        } else if (name.length() == 1) {
            loadAttribute(name.toString());
        } else {
            ctx.reportError("Cannot resolve field: " + name, name);
        }
    }

    private boolean isEvalMethod() {
        return "eval".equals(ctx.getMethodName()) && !ctx.isStatic();
    }
}
