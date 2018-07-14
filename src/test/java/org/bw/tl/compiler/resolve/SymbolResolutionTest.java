package org.bw.tl.compiler.resolve;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

import static org.bw.tl.TestUtilities.getResolver;

public class SymbolResolutionTest {

    @Test
    public void testResolveFunction() {
        final SymbolResolver resolver = getResolver("package testModule; int testFunction() {}" +
                "void anotherTest() {} \n" +
                "void funWithParams(boolean a, int b) {}\n");

        final Type modType = Type.getType("LtestModule;");

        Assert.assertEquals(Type.INT_TYPE, resolver.resolveFunction(modType, "testFunction").getTypeDescriptor());
        Assert.assertEquals(Type.VOID_TYPE, resolver.resolveFunction(modType, "anotherTest").getTypeDescriptor());

        Assert.assertEquals(Type.VOID_TYPE, resolver.resolveFunction(modType, "funWithParams", Type.BOOLEAN_TYPE,
                Type.INT_TYPE).getTypeDescriptor());

        Assert.assertNull(resolver.resolveFunction(modType, "funWithParams", Type.BOOLEAN_TYPE,
                Type.LONG_TYPE));
    }
}
