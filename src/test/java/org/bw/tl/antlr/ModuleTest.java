package org.bw.tl.antlr;

import org.bw.tl.antlr.ast.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.util.LinkedList;
import java.util.List;

public class ModuleTest {

    private Module module;

    @Before
    public void initModule() {
        final List<File> files = new LinkedList<>();

        final List<Function> funcs = new LinkedList<>();
        final List<QualifiedName> imports = new LinkedList<>();
        imports.add(new QualifiedName("java", "lang", "String"));

        final File file = new File(new QualifiedName(), imports, new LinkedList<>(), funcs, "test.tl");
        files.add(file);

        final Function func = new Function(new QualifiedName[]{new QualifiedName("int")}, new String[] { "a" },
                "testFun", new Block(new Node[0]), new QualifiedName("String"));

        funcs.add(func);
        file.getFields().add(new Field("testField", new QualifiedName("String"), new Literal<>("")));

        module = Module.of(files);
    }

    @Test
    public void resolveFunctionTest() {
        final Type type = module.resolveFunction("testFun", Type.getType("I"));
        Assert.assertNotNull(type);
        Assert.assertEquals("Ljava/lang/String;", type.getDescriptor());
    }

    @Test
    public void resolveFieldTest() {
        final Type type = module.resolveField("testField");
        Assert.assertNotNull(type);
        Assert.assertEquals("Ljava/lang/String;", type.getDescriptor());
    }
}
