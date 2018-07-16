package org.bw.tl.primer;

import org.bw.tl.Error;
import org.bw.tl.ErrorType;
import org.bw.tl.antlr.ast.*;

import java.util.Arrays;
import java.util.List;

/**
 * Prepare the nodes by applying all implicit modifiers
 */
public class ModifierPrimer extends ASTVisitorBase implements Primer {

    private final List<Modifier> ACCESS_MODIFIERS = Arrays.asList(Modifier.PUBLIC, Modifier.PRIVATE, Modifier.PROTECTED);

    @Override
    public void visitField(final Field field) {
        prime(field);
    }

    @Override
    public void visitFunction(final Function function) {
        prime(function);
    }

    private int countModifiers(final ModifiableStatement modifiable) {
        int count = 0;

        for (final Modifier modifier : modifiable.getModifiers()) {
            if (ACCESS_MODIFIERS.contains(modifier)) {
                count++;
            }
        }

        return count;
    }

    private void prime(final ModifiableStatement modifiable) {
        int count = countModifiers(modifiable);
        modifiable.addModifiers(Modifier.STATIC);
        if (count == 0) {
            modifiable.addModifiers(Modifier.PUBLIC);
        } else if (count > 1) {
            Error e = ErrorType.GENERAL_ERROR.newError("Illegal modifiers " + modifiable.getModifiers(), modifiable);
            e.print();
        }
    }

    @Override
    public void prime(final Module module) {
        module.getFiles().forEach(file -> {
            file.getFields().forEach(field -> field.accept(this));
            file.getFunctions().forEach(fun -> fun.accept(this));
        });
    }
}
