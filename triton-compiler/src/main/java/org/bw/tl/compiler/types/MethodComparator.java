package org.bw.tl.compiler.types;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Non-dominated comparison
 */
@RequiredArgsConstructor
public class MethodComparator implements Comparator<Type[]> {

    private final Type[] input;

    @Override
    public int compare(final Type[] a, final Type[] b) {
        if (a.length != b.length || Arrays.equals(a, b))
            return 0;

        int betterCount = 0;
        int worseCount = 0;

        for (int i = 0; i < a.length; i++) {
            final Comparator<Type> tc = new TypeComparator(input[i]);
            final int result = tc.compare(a[i], b[i]);

            if (result < 0)
                betterCount++;
            else if (result > 0)
                worseCount++;
        }

        if (betterCount > 0 && worseCount > 0) {
            return 0;
        } else if (betterCount > 0 && worseCount == 0) {
            return -1;
        } else if (worseCount > 0 && betterCount == 0) {
            return 1;
        }

        return 0;
    }
}
