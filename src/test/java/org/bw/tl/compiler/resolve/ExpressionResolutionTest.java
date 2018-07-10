package org.bw.tl.compiler.resolve;

import org.bw.tl.antlr.ast.*;
import org.bw.tl.util.TypeUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Type.*;

public class ExpressionResolutionTest {

    @Test
    public void testResolveBinaryOpByte() {
        final ExpressionResolverImpl expressionResolver = new ExpressionResolverImpl(null, null,
                null, null);
        final BinaryOp bop = new BinaryOp(new Literal<>(100), "*", new Literal<>(5));
        final Type type = bop.resolveType(expressionResolver);

        Assert.assertEquals(BYTE_TYPE, type);

        Assert.assertTrue(TypeUtilities.isAssignableFrom(type, INT_TYPE));
        Assert.assertTrue(TypeUtilities.isAssignableWithImplicitCast(type, Type.LONG_TYPE));
    }

    @Test
    public void testResolveBinaryOpInt() {
        final ExpressionResolverImpl expressionResolver = new ExpressionResolverImpl(null, null,
                null, null);
        final BinaryOp bop = new BinaryOp(new Literal<>(100000), "*", new Literal<>(5));
        final Type type = bop.resolveType(expressionResolver);

        Assert.assertEquals(INT_TYPE, type);

        Assert.assertTrue(TypeUtilities.isAssignableWithImplicitCast(type, Type.LONG_TYPE));
    }

    @Test
    public void resolveExpression() {
        final SymbolResolver resolver = SymbolResolutionTest.getResolver("package mod;" +
                "float abc = 100\n" +
                "java.lang.String str = \"a string lol\"\n" +
                "long add(int a, float b) {}");

        final ExpressionResolverImpl expressionResolver = new ExpressionResolverImpl(resolver, resolver.getCtx(),
                resolver.getCtx().getFiles().get(0), null);

        // expr = abc * add(60000, abc)
        // F    = F   * L(INT, FLOAT)
        Expression expr = new BinaryOp(new QualifiedName("abc"), "*", new Call(null,
                "add",
                new Expression[]{new Literal<>(60000), new QualifiedName("abc")}
        ));

        System.out.println(expr.resolveType(expressionResolver));
        Assert.assertEquals(Type.FLOAT_TYPE, expr.resolveType(expressionResolver));
    }
}
