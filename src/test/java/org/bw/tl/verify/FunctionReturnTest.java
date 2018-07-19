package org.bw.tl.verify;

import org.bw.tl.antlr.ast.Clazz;
import org.bw.tl.antlr.ast.Function;
import org.junit.Assert;
import org.junit.Test;

import static org.bw.tl.TestUtilities.getClazzFromFile;

public class FunctionReturnTest {

    @Test
    public void testReturn() {
        final Clazz module = getClazzFromFile("testData/return/FunctionReturnTest.tl");
        Assert.assertNotNull(module);

        final FunReturnVerifier rv = new FunReturnVerifier();

        for (final Function function : module.getFunctions()) {
            if (function.getName().toLowerCase().contains("valid")) {
                Assert.assertTrue(rv.isValid(function));
            } else {
                Assert.assertFalse(rv.isValid(function));
            }
        }
    }
}
