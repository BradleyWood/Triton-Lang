package org.bw.tl.compiler.resolve;

import lombok.Data;
import org.bw.tl.antlr.ast.Module;
import org.bw.tl.compiler.Compiler;
import org.bw.tl.primer.ModifierPrimer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static org.bw.tl.TestUtilities.getModuleFromFile;

@RunWith(Parameterized.class)
public @Data class SymbolResolutionTest {

    private final String filePath;

    @Test
    public void testResolveFunction() {
        final Module module = getModuleFromFile(filePath);

        Assert.assertNotNull(module);

        final ModifierPrimer modifierPrimer = new ModifierPrimer();
        modifierPrimer.prime(module);

        final Compiler compiler = new Compiler(module);

        final Map<String, byte[]> o = compiler.compile();

        Assert.assertEquals(0, compiler.getErrors().size());
        Assert.assertNotNull(o);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() throws IOException {
        final LinkedList<Object[]> parameters = new LinkedList<>();

        Files.walk(Paths.get("testData/resolution/")).filter(p -> p.toString().endsWith(".tl"))
                .map(Path::toString).forEach(p -> parameters.add(new Object[] { p }));

        return parameters;
    }
}
