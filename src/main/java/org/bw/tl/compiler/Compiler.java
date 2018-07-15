package org.bw.tl.compiler;

import lombok.Data;
import org.bw.tl.Error;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.resolve.SymbolResolver;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.*;
import java.util.stream.Collectors;

import static org.bw.tl.util.TypeUtilities.getFunctionDescriptor;
import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Opcodes.*;

public @Data class Compiler {

    private final List<Error> errors = new LinkedList<>();
    private final List<Module> modules;

    public Compiler(final List<Module> modules) {
        this.modules = modules;
    }

    public Compiler(final Module... modules) {
        this(Arrays.asList(modules));
    }

    public Map<String, byte[]> compile() {
        final HashMap<String, byte[]> classMap = new HashMap<>();

        for (final Module module : modules) {
            try {
                classMap.put(module.getModuleClassName(), build(module));
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        if (!errors.isEmpty())
            return null;

        return classMap;
    }

    private byte[] build(final Module module) {
        final ClassWriter cw = new ClassWriter(COMPUTE_FRAMES + COMPUTE_MAXS);

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, module.getInternalName(), null,
                "java/lang/Object", null);

        buildClassInitializer(cw, module);

        final SymbolResolver symbolResolver = new SymbolResolver(modules, module);

        for (final File file : module.getFiles()) {
            for (final Function function : file.getFunctions()) {
                final String methodDescriptor = getFunctionDescriptor(symbolResolver, function.getType(),
                        function.getParameterTypes());

                if (methodDescriptor == null) {
                    System.err.println("Cannot resolve method descriptor");
                    return null;
                }

                final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, function.getName(),
                        methodDescriptor, null, null);

                mv.visitCode();

                final MethodCtx ctx = new MethodCtx(modules, function, module, file);

                function.accept(MethodImpl.of(mv, ctx));

                errors.addAll(ctx.getErrors());

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }

        return cw.toByteArray();
    }

    private void buildClassInitializer(final ClassWriter cw, final Module module) {

        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "<clinit>", "()V",
                null, null);

        mv.visitCode();

        final List<Node> statements = module.getFields().stream()
                .map(f -> new BinaryOp(new QualifiedName(f.getName()), "=", f.getInitialValue()))
                .collect(Collectors.toList());

        final Block block = new Block(statements);
        final Function init = new Function(new QualifiedName[0], new String[0], "<clinit>", block,
                new QualifiedName("void"));

        final MethodCtx ctx = new MethodCtx(modules, init, module, null);

        init.accept(MethodImpl.of(mv, ctx));

        errors.addAll(ctx.getErrors());

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
