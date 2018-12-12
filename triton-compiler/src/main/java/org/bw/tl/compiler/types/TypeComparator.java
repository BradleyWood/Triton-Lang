package org.bw.tl.compiler.types;

import lombok.RequiredArgsConstructor;
import org.bw.tl.util.TypeUtilities;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.bw.tl.util.TypeUtilities.isAssignableFrom;
import static org.bw.tl.util.TypeUtilities.isAssignableWithImplicitCast;

@RequiredArgsConstructor
public class TypeComparator implements Comparator<Type> {

    private final List<Type> DISCRETE_TYPES = Arrays.asList(Type.BYTE_TYPE, Type.SHORT_TYPE, Type.INT_TYPE, Type.LONG_TYPE);
    private final List<Type> CONTINUOUS_TYPES = Arrays.asList(Type.FLOAT_TYPE, Type.DOUBLE_TYPE);

    private final Type ref;

    @Override
    public int compare(final Type a, final Type b) {
        if (Objects.equals(a, b))
            return 0;
        if (Objects.equals(ref, a))
            return -1;
        if (Objects.equals(ref, b))
            return 1;

        Primitive pa = Primitive.getPrimitiveByDesc(a.getDescriptor());
        if (pa == null)
            pa = Primitive.getPrimitiveFromWrapper(a.getDescriptor());

        Primitive pb = Primitive.getPrimitiveByDesc(b.getDescriptor());
        if (pb == null)
            pb = Primitive.getPrimitiveFromWrapper(b.getDescriptor());

        final boolean canCastToA = isAssignableFrom(ref, a) || isAssignableWithImplicitCast(ref, a);
        final boolean canCastToB = isAssignableFrom(ref, b) || isAssignableWithImplicitCast(ref, b);

        if (!canCastToA && !canCastToB)
            return 0;

        if (DISCRETE_TYPES.contains(ref)) {
            final int refIdx = DISCRETE_TYPES.indexOf(ref);

            final int diffAR = DISCRETE_TYPES.indexOf(a) - refIdx;
            final int diffBR = DISCRETE_TYPES.indexOf(b) - refIdx;

            if (diffAR > -1 && diffBR < 0)
                return -1;
            else if (diffBR > -1 && diffAR < 0)
                return 1;

            if (diffAR >= 0) {
                if (diffAR < diffBR)
                    return -1;
                else if (diffBR < diffAR)
                    return 1;
            }

            if (pa != null && !pa.getDesc().equals(a.getDescriptor()) && pa.getDesc().equals(ref.getDescriptor())) {
                return -1;
            }

            if (pb != null && !pb.getDesc().equals(b.getDescriptor()) && pb.getDesc().equals(ref.getDescriptor())) {
                return 1;
            }

            final Primitive refPrim = Primitive.getPrimitiveByDesc(ref.getDescriptor());
            final Type refWrapped = Type.getType(refPrim.getWrappedType());

            return cmpObjects(refWrapped, a, b);
        } else if (CONTINUOUS_TYPES.contains(ref)) {
            if (Type.FLOAT_TYPE.equals(ref) && Type.getType(Float.class).equals(a)) {
                return -1;
            } else if (Type.FLOAT_TYPE.equals(ref) && Type.getType(Float.class).equals(b)) {
                return 1;
            }
        } else {
            if (pa != null && pa.getDesc().equals(ref.getDescriptor())) {
                return -1;
            } else if (pb != null && pb.getDesc().equals(ref.getDescriptor())) {
                return 1;
            }

            return cmpObjects(ref, a, b);
        }

        return 0;
    }

    private static int cmpObjects(final Type ref, final Type a, final Type b) {
        final int aParentCount = TypeUtilities.countToParent(ref, a);
        final int bParentCount = TypeUtilities.countToParent(ref, b);

        if (aParentCount != -1 && bParentCount == -1)
            return -1;
        if (bParentCount != -1 && aParentCount == -1)
            return 1;

        if (aParentCount != -1) {
            if (aParentCount < bParentCount)
                return -1;
            if (bParentCount < aParentCount)
                return 1;
        }

        return 0;
    }
}
