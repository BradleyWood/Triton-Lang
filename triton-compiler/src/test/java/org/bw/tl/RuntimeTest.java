package org.bw.tl;

import lombok.Data;
import org.bw.tl.util.CompileUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public @Data class RuntimeTest {

    private final Method method;
    private final String name;

    @Test
    public void test() throws InvocationTargetException, IllegalAccessException {
        method.invoke(null);
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> parameters() {
        final LinkedList<Object[]> parameters = new LinkedList<>();

        try {
            final Set<String> files = Files.walk(Paths.get("testData/rt_tests/"))
                    .filter(p -> p.toString().endsWith(".tl"))
                    .map(Path::toString).collect(Collectors.toSet());

            for (final String file : files) {
                final Map<String, byte[]> map = CompileUtilities.compile(Collections.singletonList(file));
                Assert.assertNotNull("Test class failed to compile: " + file, map);

                for (final Map.Entry<String, byte[]> entry : map.entrySet()) {
                    Assert.assertNotNull("Test class failed to compile: "+ entry.getKey(), entry.getValue());

                    final Class<?> cl = TestUtilities.loadClass(entry.getKey(), entry.getValue());
                    Assert.assertNotNull("Error loading class", cl);

                    try {
                        for (final Method method : cl.getDeclaredMethods()) {
                            if (method.getName().toLowerCase().contains("test") && Modifier.isStatic(method.getModifiers())) {
                                parameters.add(new Object[]{method, cl.getName() + "." + method.getName() + "()"});
                            }
                        }

                    } catch (VerifyError e) {
                        Assert.fail("Error loading class: " + entry.getKey());
                    }
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return parameters;
    }
}
