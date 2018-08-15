package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.QualifiedName;
import org.bw.tl.antlr.ast.TypeCast;

@RequiredArgsConstructor(staticName = "of")
public class TypeCastVisitor extends GrammarBaseVisitor<TypeCast> {

    private final @Getter String sourceFile;

    @Override
    public TypeCast visitTypeCast(final GrammarParser.TypeCastContext ctx) {
        final QualifiedName type = QualifiedName.of(ctx.type().getText());
        final Expression expression = ctx.expression().accept(ExpressionVisitor.of(sourceFile));

        final TypeCast typeCast = new TypeCast(type, expression);

        type.setParent(typeCast);
        expression.setParent(typeCast);

        return typeCast;
    }
}
