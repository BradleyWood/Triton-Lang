package org.bw.tl.compiler;

import lombok.Data;
import org.bw.tl.Error;
import org.bw.tl.ErrorType;
import org.bw.tl.antlr.ast.*;
import org.bw.tl.compiler.resolve.ExpressionResolver;
import org.bw.tl.compiler.resolve.ExpressionResolverImpl;
import org.bw.tl.util.TypeUtilities;
import org.bw.tl.verify.FunReturnVerifier;
import org.bw.tl.verify.Verifiable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.stream.Collectors;

import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Opcodes.*;

public @Data class Compiler {

    private final Verifiable<Function> functionVerifiable = new FunReturnVerifier();
    private final List<Error> errors = new LinkedList<>();
    private final List<Clazz> classes;
    private final ClassLoader loader;
    private String parent = "java/lang/Object";

    public Compiler(final List<Clazz> classes, final ClassLoader loader) {
        this.classes = classes;

        if (loader == null) {
            this.loader = Compiler.class.getClassLoader();
        } else {
            this.loader = loader;
        }
    }

    public Compiler(final Clazz... classes) {
        this(Arrays.asList(classes), null);
    }

    public Map<String, byte[]> compile() {
        final HashMap<String, byte[]> classMap = new HashMap<>();

        for (final Clazz clazz : classes) {
            try {
                classMap.put(clazz.getModuleClassName(), build(clazz));
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        if (!errors.isEmpty())
            return null;

        return classMap;
    }

    private byte[] build(final Clazz clazz) {
        final ClassWriter cw = new ClassWriter(COMPUTE_FRAMES + COMPUTE_MAXS);

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, clazz.getInternalName(), null,
                parent, null);

        buildClassInitializer(cw, clazz);

        final ExpressionResolver resolver = new ExpressionResolverImpl(clazz, classes, loader, new Scope());

        for (final Field field : clazz.getFields()) {
            if (field.getType() == null) {
                errors.add(ErrorType.GENERAL_ERROR.newError("Type inference is not supported for fields", field));
                continue;
            }

            final Type type = field.getType().resolveType(resolver);

            if (type == null) {
                errors.add(ErrorType.GENERAL_ERROR.newError("Cannot resolve type: " + field.getType(), field));
                continue;
            }
            cw.visitField(field.getAccessModifiers(), field.getName(), type.getDescriptor(), null, null);
        }

        final Set<String> methodSignatures = new HashSet<>();

        for (final Function function : clazz.getFunctions()) {
            final Type methodDescriptor = resolver.resolveFunctionCtx(clazz, function);

            if (!functionVerifiable.isValid(function)) {
                errors.add(ErrorType.GENERAL_ERROR.newError("Missing return statement", function));
                continue;
            }

            if (methodDescriptor == null) {
                errors.add(ErrorType.GENERAL_ERROR.newError("Invalid method signature", function));
                continue;
            }

            final String sig = TypeUtilities.getMethodSignature(function.getName(), methodDescriptor);

            if (methodSignatures.contains(sig)) {
                errors.add(ErrorType.GENERAL_ERROR.newError("Duplicate method signature: " + sig, function));
                continue;
            }

            methodSignatures.add(sig);

            MethodVisitor mv = cw.visitMethod(function.getAccessModifiers(), function.getName(),
                    methodDescriptor.getDescriptor(), null, null);

            mv.visitCode();

            final MethodCtx ctx = new MethodCtx(classes, function, clazz, loader);

            final MethodImpl methodImpl = new MethodImpl(mv, ctx);
            function.accept(methodImpl);

            mv.visitMaxs(0, 0);
            mv.visitEnd();

            errors.addAll(ctx.getErrors());

            final LinkedList<Function> syntheticMethods = new LinkedList<>(ctx.getSyntheticASTMethods());
            final LinkedList<MethodNode> syntheticASMMethods = new LinkedList<>(ctx.getSyntheticASMMethods());

            while (!syntheticMethods.isEmpty()) {
                final Function syntheticMethod = syntheticMethods.removeFirst();

                if (!functionVerifiable.isValid(syntheticMethod)) {
                    errors.add(ErrorType.GENERAL_ERROR.newError("Missing return value", clazz.getSourceFile(), -1));
                    continue;
                }

                final Type syntheticMethodType = resolver.resolveFunctionCtx(clazz, syntheticMethod);
                final MethodCtx syntheticCtx = new MethodCtx(classes, syntheticMethod, clazz, loader);
                syntheticCtx.setSynthetic(true);

                mv = cw.visitMethod(syntheticMethod.getAccessModifiers(), syntheticMethod.getName(),
                        syntheticMethodType.getDescriptor(), null, null);

                mv.visitCode();

                final MethodImpl impl = new MethodImpl(mv, syntheticCtx);
                syntheticMethod.accept(impl);

                syntheticMethods.addAll(syntheticCtx.getSyntheticASTMethods());
                syntheticASMMethods.addAll(syntheticCtx.getSyntheticASMMethods());
                errors.addAll(syntheticCtx.getErrors());
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            for (final MethodNode syntheticASMMethod : syntheticASMMethods) {
                mv = cw.visitMethod(syntheticASMMethod.access, syntheticASMMethod.name,
                        syntheticASMMethod.desc, syntheticASMMethod.signature, null);
                syntheticASMMethod.accept(mv);
            }
        }

        return cw.toByteArray();
    }

    private void buildClassInitializer(final ClassWriter cw, final Clazz clazz) {

        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "<clinit>", "()V",
                null, null);

        mv.visitCode();

        final List<Node> statements = clazz.getFields().stream().filter(stmt -> stmt.getType() != null)
                .map(f -> new Assignment(null, f.getName(), f.getInitialValue()))
                .collect(Collectors.toList());

        final Block block = new Block(statements);
        block.getStatements().addAll(clazz.getScheduleBlocks());

        final Function init = new Function(new TypeName[0], new String[0], new List[0], "<clinit>", block,
                new TypeName("void"));

        final MethodCtx ctx = new MethodCtx(classes, init, clazz, loader);

        final MethodImpl methodImpl = new MethodImpl(mv, ctx);
        init.accept(methodImpl);

        errors.addAll(ctx.getErrors());

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
