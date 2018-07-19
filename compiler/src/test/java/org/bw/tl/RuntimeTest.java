package org.bw.tl;

import lombok.Data;
import org.bw.tl.antlr.ast.Clazz;
import org.bw.tl.antlr.ast.QualifiedName;
import org.bw.tl.primer.ModifierPrimer;
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

import org.bw.tl.compiler.Compiler;

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
            final ModifierPrimer mp = new ModifierPrimer();

            final List<Clazz> moduleList = Files.walk(Paths.get("testData/rt_tests/"))
                    .filter(p -> p.toString().endsWith(".tl"))
                    .map(Path::toString)
                    .map(TestUtilities::getClazzFromFile)
                    .peek(mp::prime)
                    .collect(Collectors.toList());

            for (final Clazz clazz : moduleList) {
                clazz.getStaticImports().add(QualifiedName.of("org.bw.tl.Builtin"));
                final Compiler compiler = new Compiler(clazz);
                final Map<String, byte[]> classMap = compiler.compile();
                for (final Error error : compiler.getErrors()) {
                    error.print();
                }
                Assert.assertNotNull("Test class failed to compile", classMap);
                Assert.assertTrue("Test class failed to compile", compiler.getErrors().isEmpty());
                for (final Map.Entry<String, byte[]> entry : classMap.entrySet()) {
                    final Class<?> cl = TestUtilities.loadClass(entry.getKey(), entry.getValue());
                    Assert.assertNotNull("Error loading class", cl);
                    for (final Method method : cl.getDeclaredMethods()) {
                        if (method.getName().toLowerCase().contains("test") && Modifier.isStatic(method.getModifiers())) {
                            parameters.add(new Object[]{method, cl.getName() + "." + method.getName() + "()"});
                        }
                    }
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return parameters;
    }
}
