package org.bw.tl.primer;

import org.bw.tl.antlr.ast.Module;


public interface Primer {

    /**
     * Prime the program for code generation by adding some implicit logic
     *
     * @param file The file to prime
     */
    void prime(final Module file);



}
