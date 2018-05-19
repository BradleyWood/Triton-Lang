package org.bw.tl.compiler.types;

import org.junit.Assert;
import org.junit.Test;

public class TypeTest {

    @Test
    public void testInternalNameCalculation() {
        final String typeDesc = "Ljava/lang/String;";
        Assert.assertEquals("java/lang/String", new AnyType(typeDesc).getInternalName());

        Assert.assertEquals("I", IntType.INSTANCE.getInternalName());
        Assert.assertEquals("I",  new AnyType("I").getInternalName());
    }
}
