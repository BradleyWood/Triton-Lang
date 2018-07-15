package org.bw.tl.compiler.resolve;

import org.bw.tl.antlr.ast.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.bw.tl.TestUtilities.getResolver;
import static org.bw.tl.TestUtilities.parseExpression;
import static org.bw.tl.util.TypeUtilities.getTypeFromName;
import static org.bw.tl.util.TypeUtilities.isAssignableFrom;
import static org.bw.tl.util.TypeUtilities.isAssignableWithImplicitCast;
import static org.objectweb.asm.Type.*;

@RunWith(Enclosed.class)
public class ExpressionResolutionTest {

    public static class ModuleExpressionResolutionTest {
        @Test
        public void testResolveBinaryOpByte() {
            final ExpressionResolverImpl expressionResolver = new ExpressionResolverImpl(null, null,
                    null, null);
            final BinaryOp bop = new BinaryOp(new Literal<>(100), "*", new Literal<>(5));
            final Type type = bop.resolveType(expressionResolver);

            Assert.assertEquals(BYTE_TYPE, type);

            Assert.assertTrue(isAssignableFrom(type, INT_TYPE));
            Assert.assertTrue(isAssignableWithImplicitCast(type, Type.LONG_TYPE));
        }

        @Test
        public void testResolveBinaryOpInt() {
            final ExpressionResolverImpl expressionResolver = new ExpressionResolverImpl(null, null,
                    null, null);
            final BinaryOp bop = new BinaryOp(new Literal<>(100000), "*", new Literal<>(5));
            final Type type = bop.resolveType(expressionResolver);

            Assert.assertEquals(INT_TYPE, type);

            Assert.assertTrue(isAssignableWithImplicitCast(type, Type.LONG_TYPE));
        }

        @Test
        public void resolveExpression() {
            final SymbolResolver resolver = getResolver("package mod;" +
                    "float abc = 100\n" +
                    "java.lang.String str = \"a string lol\"\n" +
                    "fun add(int a, float b): long {}");

            final ExpressionResolverImpl expressionResolver = new ExpressionResolverImpl(resolver, resolver.getCtx(),
                    resolver.getCtx().getFiles().get(0), null);

            // expr = abc * add(60000, abc)
            // F    = F   * L(INT, FLOAT)
            Expression expr = new BinaryOp(new QualifiedName("abc"), "*", new Call(null,
                    "add",
                    new Expression[]{new Literal<>(60000), new QualifiedName("abc")}
            ));

            Assert.assertEquals(Type.FLOAT_TYPE, expr.resolveType(expressionResolver));
        }
    }

    @RunWith(Parameterized.class)
    public static class SimpleResolutionTest {

        private final String expectedType;
        private final String expr;
        private final boolean requireExactMatch;
        private final boolean shouldFail;

        public SimpleResolutionTest(final String expectedType, final String expr, final boolean requireExactMatch,
                                    final boolean shouldFail) {
            this.expectedType = expectedType;
            this.expr = expr;
            this.requireExactMatch = requireExactMatch;
            this.shouldFail = shouldFail;
        }

        @Test
        public void resolveTest() {
            final ExpressionResolverImpl resolver = new ExpressionResolverImpl(null, null, null, null);

            final Expression expression = parseExpression(expr);
            final Type resolvedType = expression.resolveType(resolver);

            Assert.assertNotNull("Failed to parse expression", expression);

            if (shouldFail) {
                Assert.assertNull("Resolution should have failed", resolvedType);
            } else {
                final Type type = getTypeFromName(expectedType);
                Assert.assertNotNull(type);
                Assert.assertNotNull(expression);

                if (requireExactMatch) {
                    Assert.assertEquals(type, resolvedType);
                } else {
                    final boolean eq = type.equals(resolvedType);
                    final boolean isAssignable = isAssignableFrom(resolvedType, type);
                    final boolean isAssignableWithImplicitCast = isAssignableWithImplicitCast(resolvedType, type);
                    Assert.assertTrue(eq || isAssignable || isAssignableWithImplicitCast);
                }
            }
        }

        @Parameterized.Parameters(name = "Resolution Test: {1} -> {0}")
        public static Collection<Object[]> parameters() throws IOException {
            final LinkedList<Object[]> parameters = new LinkedList<>();

            final List<String> lines = Files.readAllLines(Paths.get("testData/resolution/expressions.resolve"));

            for (String line : lines) {
                line = line.trim();

                if (line.isEmpty())
                    continue;

                boolean requireExact = true;

                try {
                    String type = null;
                    String expr = null;
                    if (line.contains("~:")) {
                        String[] sp = line.split("~:");
                        requireExact = false;
                        type = sp[0].trim();
                        expr = sp[1].trim();
                    } else if (line.contains(":")) {
                        String[] sp = line.split(":");
                        type = sp[0].trim();
                        expr = sp[1].trim();
                    } else if (line.startsWith("!!")) {
                        expr = line.substring(line.indexOf("!!")).trim();
                    }

                    parameters.add(new Object[]{type, expr, requireExact, false});
                } catch (Throwable e) {
                    parameters.add(new Object[]{"", line, requireExact, false});
                }
            }

            return parameters;
        }
    }
}
