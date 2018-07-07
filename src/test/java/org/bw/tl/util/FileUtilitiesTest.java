package org.bw.tl.util;

import org.bw.tl.antlr.ast.QualifiedName;
import org.junit.Assert;
import org.junit.Test;

public class FileUtilitiesTest {

    @Test
    public void testGetTypeFromName() {
        Assert.assertNotNull(FileUtilities.getTypeFromName(new QualifiedName("java", "lang", "String")));
        Assert.assertNotNull(FileUtilities.getTypeFromName(new QualifiedName("java", "lang", "Integer")));

        Assert.assertNotNull(FileUtilities.getTypeFromName(new QualifiedName("int")));
    }
}
