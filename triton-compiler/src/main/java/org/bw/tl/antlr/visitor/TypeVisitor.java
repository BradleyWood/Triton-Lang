package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.AllArgsConstructor;
import org.bw.tl.antlr.ast.TypeName;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.GrammarBaseVisitor;

@AllArgsConstructor(staticName = "of")
public class TypeVisitor extends GrammarBaseVisitor<TypeName> {

    private final @Getter String sourceFile;

    @Override
    public TypeName visitType(final GrammarParser.TypeContext ctx) {
        if (ctx.typeArguments() == null)
            return TypeName.of(ctx.getText());

        final TypeName type = TypeName.of(ctx.fqn().getText());

        if (ctx.typeArguments().typeArgument() != null) {
            for (final GrammarParser.TypeArgumentContext typeArgumentContext : ctx.typeArguments().typeArgument()) {
                type.addTypeParameter(TypeName.of(typeArgumentContext.getText()));
            }
        }

        return type;
    }
}
