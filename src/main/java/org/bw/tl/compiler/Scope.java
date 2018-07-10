package org.bw.tl.compiler;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Stack;

public class Scope {

    private final Stack<ArrayList<Var>> scope = new Stack<>();

    public void beginScope() {
        scope.push(new ArrayList<>());
    }

    public void endScope() {
        scope.pop();
    }

    public boolean putVar(@NotNull final String name, @NotNull final Type type) {
        if (findVar(name) != null) {
            return false;
        }

        int idx = 0;
        for (final ArrayList<Var> vars : scope) {
            idx += vars.size();
        }

        final Var var = new Var(name, type, idx);

        scope.peek().add(var);
        return true;
    }

    public Var findVar(@NotNull final String name) {
        for (final ArrayList<Var> list : scope) {
            for (final Var var : list) {
                if (var.getName().equals(name)) {
                    return var;
                }
            }
        }
        return null;
    }

    public int count() {
        return scope.size();
    }

    public void clear() {
        scope.clear();
    }

    public static @Data class Var {
        private final @NotNull String name;
        private final @NotNull Type type;
        private final int index;
    }
}