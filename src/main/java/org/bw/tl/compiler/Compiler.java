package org.bw.tl.compiler;

import lombok.Data;
import org.bw.tl.Error;
import org.bw.tl.ErrorType;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.resolve.SymbolResolver;
import org.bw.tl.verify.FunReturnVerifier;
import org.bw.tl.verify.Verifiable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.stream.Collectors;

import static org.bw.tl.util.FileUtilities.getType;
import static org.bw.tl.util.TypeUtilities.getFunctionDescriptor;
import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Opcodes.*;

public @Data class Compiler {

    private final Verifiable<Function> functionVerifiable = new FunReturnVerifier();
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

        for (final File file : module.getFiles()) {
            final SymbolResolver symbolResolver = new SymbolResolver(modules, module, file);

            for (final Field field : file.getFields()) {
                if (field.getType() == null) {
                    errors.add(ErrorType.GENERAL_ERROR.newError("Implicit typing is only supported for local variables", field));
                    continue;
                }
                final Type type = getType(file, field.getType());
                if (type == null) {
                    errors.add(ErrorType.GENERAL_ERROR.newError("Cannot resolve type: " + field.getType(), field));
                    continue;
                }
                cw.visitField(field.getAccessModifiers(), field.getName(), type.getDescriptor(), null, null);
            }

            for (final Function function : file.getFunctions()) {
                final String methodDescriptor = getFunctionDescriptor(symbolResolver, function.getType(),
                        function.getParameterTypes());

                if (!functionVerifiable.isValid(function)) {
                    errors.add(ErrorType.GENERAL_ERROR.newError("Missing return statement", function));
                    continue;
                }

                if (methodDescriptor == null) {
                    errors.add(ErrorType.GENERAL_ERROR.newError("Invalid method signature", function));
                    continue;
                }

                final MethodVisitor mv = cw.visitMethod(function.getAccessModifiers(), function.getName(),
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

        final List<Node> statements = module.getFields().stream().filter(stmt -> stmt.getType() != null)
                .map(f -> new Assignment(null, f.getName(), f.getInitialValue()))
                .collect(Collectors.toList());

        final Block block = new Block(statements);
        final Function init = new Function(new QualifiedName[0], new String[0], "<clinit>", block,
                new QualifiedName("void"));

        final MethodCtx ctx = new MethodCtx(modules, init, module, module.getFiles().get(0));

        init.accept(MethodImpl.of(mv, ctx));

        errors.addAll(ctx.getErrors());

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
