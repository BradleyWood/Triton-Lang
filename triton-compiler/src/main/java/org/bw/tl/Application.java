package org.bw.tl;

import org.bw.tl.util.CompileUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Application {

    public static void main(final String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: titonc <src_dir> <out_dir>");
            System.exit(1);
        }

        final String[] classpath = {
                "C:\\Users\\brad\\IdeaProjects\\test-lang\\triton-stdlib\\target\\triton-stdlib-1.0-SNAPSHOT.jar"
        };

        final Map<String, byte[]> classes = CompileUtilities.compile(args[0], classpath);

        if (classes == null) {
            System.err.println("Compilation Failed!");
            System.exit(1);
        } else {
            final File parent = new File(args[1]);

            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    System.err.println("Cannot create folder: " + parent);
                    System.exit(1);
                }
            }

            for (final Map.Entry<String, byte[]> entry : classes.entrySet()) {
                final File classFile = new File(parent,  entry.getKey().replace(".", "//") + ".class");

                if (!classFile.getParentFile().exists()) {
                    if (!classFile.getParentFile().mkdirs()) {
                        System.err.println("Cannot create folder: " + classFile.getParentFile());
                        System.exit(1);
                    }
                }

                final FileOutputStream fos = new FileOutputStream(classFile);
                fos.write(entry.getValue());
                fos.close();
            }

            System.out.printf("%d files successfully compiled!", classes.size());
        }
    }
}
