package com.github.bradleywood;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.bw.tl.util.CompileUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class CompileMojo extends AbstractMojo {

    @Parameter(name = "sourceDirectory", defaultValue = "${project.basedir}/src/main/raven")
    private String sourceDirectory;

    @Parameter(defaultValue = "${project.compileClasspathElements}", required = true, readonly = true)
    private List<String> classpath;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true, readonly = true)
    private String outputDirectory;

    @Override
    public void execute() throws CompilationFailureException {
        try {
            final Map<String, byte[]> classMap = CompileUtilities.compile(sourceDirectory, classpath);

            if (classMap == null)
                throw new CompilationFailureException();

            for (final Map.Entry<String, byte[]> entry : classMap.entrySet()) {
                final File file = new File(outputDirectory, entry.getKey().replace(".", "/") + ".class");
                final File parent = file.getParentFile();

                if (!parent.exists())
                    parent.mkdirs();

                final FileOutputStream fos = new FileOutputStream(file);
                fos.write(entry.getValue());
                fos.close();
            }
        } catch (IOException e) {
            throw new CompilationFailureException();
        }
    }
}
