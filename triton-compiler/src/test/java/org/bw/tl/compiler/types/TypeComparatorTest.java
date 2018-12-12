package org.bw.tl.compiler.types;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

public class TypeComparatorTest {

    @Test
    public void typeComparePrimitivesTest() {
        final TypeComparator tc = new TypeComparator(Type.INT_TYPE);

        Assert.assertEquals(-1, tc.compare(Type.LONG_TYPE, Type.getType(Integer.class)));
        Assert.assertEquals(-1, tc.compare(Type.INT_TYPE, Type.getType(Integer.class)));
        Assert.assertEquals(1, tc.compare(Type.getType(Integer.class), Type.INT_TYPE));

        Assert.assertEquals(1, tc.compare(Type.SHORT_TYPE, Type.getType(Integer.class)));
        Assert.assertEquals(1, tc.compare(Type.getType(Integer.class), Type.LONG_TYPE));

        Assert.assertEquals(0, tc.compare(Type.getType(Integer.class), Type.getType(Integer.class)));
        Assert.assertEquals(0, tc.compare(Type.getType(String.class), Type.getType(Void.class)));
        Assert.assertEquals(1, tc.compare(Type.getType(String.class), Type.getType(Object.class)));

        final TypeComparator tcb = new TypeComparator(Type.BYTE_TYPE);

        Assert.assertEquals(-1, tcb.compare(Type.SHORT_TYPE, Type.INT_TYPE));
        Assert.assertEquals(1, tcb.compare(Type.INT_TYPE, Type.SHORT_TYPE));
        Assert.assertEquals(0, tcb.compare(Type.FLOAT_TYPE, Type.DOUBLE_TYPE));
    }

    @Test
    public void typeCmpObjects() {
        final TypeComparator tc = new TypeComparator(Type.getType(CharSequence.class));

        Assert.assertEquals(-1, tc.compare(Type.getType(CharSequence.class), Type.getType(String.class)));
        Assert.assertEquals(1, tc.compare(Type.getType(String.class), Type.getType(CharSequence.class)));
        Assert.assertEquals(0, tc.compare(Type.getType(String.class), Type.getType(String.class)));
        Assert.assertEquals(0, tc.compare(Type.getType(Number.class), Type.getType(getClass())));


        final TypeComparator tcs = new TypeComparator(Type.getType(String.class));

        Assert.assertEquals(1, tcs.compare(Type.getType(CharSequence.class), Type.getType(String.class)));
        Assert.assertEquals(-1, tcs.compare(Type.getType(CharSequence.class), Type.getType(Number.class)));
    }
}
