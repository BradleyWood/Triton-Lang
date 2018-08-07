package org.bw.tl.util;

import org.bw.tl.antlr.ast.QualifiedName;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

import static org.bw.tl.util.TypeUtilities.getTypeFromName;

public class TypeUtilitiesTest {

    @Test
    public void testGetTypeFromName() {
        Assert.assertNotNull(getTypeFromName(new QualifiedName("java", "lang", "String")));
        Assert.assertNotNull(getTypeFromName(new QualifiedName("java", "lang", "Integer")));

        Assert.assertNotNull(getTypeFromName(new QualifiedName("int")));

        Assert.assertNotNull(getTypeFromName("byte"));
        Assert.assertNotNull(getTypeFromName("java.lang.Object"));

        Assert.assertEquals(getTypeFromName(new QualifiedName("java", "lang", "Integer")),
                getTypeFromName("java.lang.Integer"));
    }

    @Test
    public void testIsAssignable() {
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("long"), getTypeFromName("long")));
        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("int"), getTypeFromName("long")));
        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("short"), getTypeFromName("long")));
        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("byte"), getTypeFromName("long")));
        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("char"), getTypeFromName("long")));

        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("long"), getTypeFromName("int")));
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("int"), getTypeFromName("int")));
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("short"), getTypeFromName("int")));
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("char"), getTypeFromName("int")));
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("byte"), getTypeFromName("int")));

        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("long"), getTypeFromName("short")));
        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("int"), getTypeFromName("short")));
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("short"), getTypeFromName("short")));
        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("char"), getTypeFromName("short")));
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("byte"), getTypeFromName("short")));


        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("int"), getTypeFromName("boolean")));
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("boolean"), getTypeFromName("boolean")));

        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("float"), getTypeFromName("double")));
        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("double"), getTypeFromName("float")));
    }

    @Test
    public void testIsAssignableNonPrimitives() {
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("java.lang.String"), getTypeFromName("java.lang.Object")));
        Assert.assertFalse(TypeUtilities.isAssignableFrom(getTypeFromName("java.lang.Object"), getTypeFromName("java.lang.String")));
        Assert.assertTrue(TypeUtilities.isAssignableFrom(getTypeFromName("java.lang.Integer"), getTypeFromName("java.lang.Number")));
    }

    @Test
    public void testIsAssignableWithImplicitCast() {
        Assert.assertTrue(TypeUtilities.isAssignableWithImplicitCast(getTypeFromName("float"), getTypeFromName("double")));
        Assert.assertFalse(TypeUtilities.isAssignableWithImplicitCast(getTypeFromName("double"), getTypeFromName("float")));

        Assert.assertTrue(TypeUtilities.isAssignableWithImplicitCast(getTypeFromName("int"), getTypeFromName("long")));
        Assert.assertFalse(TypeUtilities.isAssignableWithImplicitCast(getTypeFromName("long"), getTypeFromName("int")));

        Assert.assertFalse(TypeUtilities.isAssignableWithImplicitCast(getTypeFromName("double"), getTypeFromName("double")));
        Assert.assertFalse(TypeUtilities.isAssignableWithImplicitCast(getTypeFromName("int"), getTypeFromName("int")));
        Assert.assertFalse(TypeUtilities.isAssignableWithImplicitCast(getTypeFromName("byte"), getTypeFromName("byte")));
    }

    @Test
    public void setDimTest() {
        final Type type = Type.getType(String.class);
        final Type arrayType2d = Type.getType(String[][].class);
        final Type arrayType3d = Type.getType(String[][][].class);

        Assert.assertEquals(arrayType3d, TypeUtilities.setDim(type, 3));
        Assert.assertEquals(arrayType2d, TypeUtilities.setDim(arrayType2d, 2));

        Assert.assertEquals(type, TypeUtilities.setDim(arrayType2d, 0));
    }
}
