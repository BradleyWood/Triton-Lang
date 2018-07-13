package org.bw.tl.compiler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.antlr.ast.ASTVisitorBase;
import org.bw.tl.antlr.ast.Function;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@EqualsAndHashCode(callSuper = false)
public @Data(staticConstructor = "of") class MethodImpl extends ASTVisitorBase implements Opcodes {

    private final @NotNull MethodVisitor mv;
    private final @NotNull MethodCtx ctx;

    @Override
    public void visitFunction(final Function function) {

        mv.visitInsn(RET);
    }
}
