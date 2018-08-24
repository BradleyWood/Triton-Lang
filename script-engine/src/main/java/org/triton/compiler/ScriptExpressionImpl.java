package org.triton.compiler;

import org.bw.tl.antlr.ast.Assignment;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.QualifiedName;
import org.bw.tl.compiler.ExpressionImpl;
import org.bw.tl.compiler.MethodCtx;
import org.bw.tl.compiler.resolve.ExpressionResolverImpl;
import org.bw.tl.compiler.resolve.FieldContext;
import org.bw.tl.compiler.types.Primitive;
import org.bw.tl.compiler.types.TypeHandler;
import org.bw.tl.util.TypeUtilities;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.script.ScriptContext;
import java.util.HashSet;

public class ScriptExpressionImpl extends ExpressionImpl {

    private final HashSet<String> varSet = new HashSet<>();

    private final MethodVisitor mv;
    private final MethodCtx ctx;

    ScriptExpressionImpl(final MethodVisitor mv, final MethodCtx ctx) {
        super(mv, ctx);
        this.mv = mv;
        this.ctx = ctx;
    }

    private boolean isEvalMethod() {
        return "eval".equals(ctx.getMethodName()) && !ctx.isStatic();
    }

    private void storeAttribute(final String name, final Expression expression) {
        final String putDesc = "(Ljava/lang/String;Ljava/lang/Object;I)V";
        final int bindingsIdx = ctx.getScope().findVar(" __ctx__ ").getIndex();

        final Type type = expression.resolveType(ctx.getResolver());
        final TypeHandler handler = TypeUtilities.getTypeHandler(type);

        mv.visitVarInsn(ALOAD, bindingsIdx);

        mv.visitLdcInsn(name);

        expression.accept(this);

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
            storeAttribute(assignment.getName(), assignment.getValue());
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
}
