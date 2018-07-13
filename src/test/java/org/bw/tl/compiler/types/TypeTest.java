package org.bw.tl.compiler.types;

import org.junit.Assert;
import org.junit.Test;

public class TypeTest {

    @Test
    public void testInternalNameCalculation() {
        final String typeDesc = "Ljava/lang/String;";
        Assert.assertEquals("java/lang/String", new AnyTypeHandler(typeDesc).getInternalName());

        Assert.assertEquals("I", IntHandler.INSTANCE.getInternalName());
        Assert.assertEquals("I",  new AnyTypeHandler("I").getInternalName());
    }
}
