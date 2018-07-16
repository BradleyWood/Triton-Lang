package org.bw.tl.verify;

import org.bw.tl.antlr.ast.Function;
import org.bw.tl.antlr.ast.Module;
import org.junit.Assert;
import org.junit.Test;

import static org.bw.tl.TestUtilities.getModuleFromFile;

public class FunctionReturnTest {

    @Test
    public void testReturn() {
        final Module module = getModuleFromFile("testData/return/FunctionReturnTest.tl");
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
