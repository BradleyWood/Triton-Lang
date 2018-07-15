package org.bw.tl.antlr.visitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Expression;
import org.bw.tl.antlr.ast.New;
import org.bw.tl.antlr.ast.QualifiedName;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor(staticName = "of")
public class NewVisitor extends GrammarBaseVisitor<New> {

    private final @Getter String sourceFile;

    @Override
    public New visitNewStatement(final GrammarParser.NewStatementContext ctx) {
        final QualifiedName name = QualifiedName.of(ctx.fqn().getText());
        final List<Expression> expressions = new LinkedList<>();

        final New newStmt = new New(name, expressions);
        name.setParent(newStmt);

        if (ctx.expression() != null) {
            ctx.expression().stream().map(e -> e.accept(ExpressionVisitor.of(sourceFile))).peek(e -> e.setParent(newStmt)).
                    forEach(expressions::add);
        }

        return newStmt;
    }

}
