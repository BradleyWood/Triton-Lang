package org.bw.tl.primer;

import org.bw.tl.antlr.ast.Clazz;

public interface Primer {

    /**
     * Prime the program for code generation by adding some implicit logic
     *
     * @param file The clazz to prime
     */
    void prime(final Clazz file);



}
