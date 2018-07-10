package org.bw.tl.compiler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;

public class ScopeTest {

    private Scope scope;

    @Before
    public void setup() {
        scope = new Scope();
    }

    @Test
    public void testScopeCreation() {
        Assert.assertEquals(0, scope.count());
        scope.beginScope();
        Assert.assertEquals(1, scope.count());
        scope.endScope();
        Assert.assertEquals(0, scope.count());
        scope.beginScope();
        scope.beginScope();
        scope.beginScope();
        scope.endScope();
        Assert.assertEquals(2, scope.count());
        scope.endScope();
        scope.endScope();
        Assert.assertEquals(0, scope.count());
    }

    @Test(expected = RuntimeException.class)
    public void testScopeUnderflow() {
        scope.beginScope();
        scope.endScope();
        scope.endScope();
    }

    @Test
    public void testVars1() {
        scope.beginScope();

        scope.putVar("test", Type.INT_TYPE);

        final Scope.Var var = scope.findVar("test");

        Assert.assertNotNull(var);
        Assert.assertEquals(0, var.getIndex());


        scope.endScope();
    }

    @Test
    public void testVars2() {
        scope.beginScope();

        scope.putVar("test", Type.BOOLEAN_TYPE);
        Assert.assertEquals(0, scope.findVar("test").getIndex());
        scope.putVar("testing", Type.INT_TYPE);
        Assert.assertEquals(1, scope.findVar("testing").getIndex());

        scope.endScope();
    }

    @Test
    public void testVars3() {
        scope.beginScope();

        scope.putVar("test", Type.getType("Ljava/lang/String;"));
        Assert.assertEquals(0, scope.findVar("test").getIndex());

        scope.beginScope();
        scope.putVar("testing", Type.getType("Ljava/lang/Object;"));
        Assert.assertEquals(Type.getType("Ljava/lang/Object;"), scope.findVar("testing").getType());
        scope.endScope();

        Assert.assertEquals(0, scope.findVar("test").getIndex());

        Assert.assertNull(scope.findVar("testing"));

        scope.endScope();
    }

    @Test
    public void testVarOverload() {
        scope.beginScope();

        scope.putVar("test", Type.INT_TYPE);

        Assert.assertEquals(0, scope.findVar("test").getIndex());

        scope.beginScope();

        Assert.assertFalse(scope.putVar("test", Type.SHORT_TYPE));

        scope.endScope();

        scope.endScope();
        scope.clear();
    }
}
