package org.triton.antlr.visitor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Node;
import org.bw.tl.antlr.visitor.FieldVisitor;
import org.bw.tl.antlr.visitor.StatementVisitor;

@EqualsAndHashCode(callSuper = true)
public @Data(staticConstructor = "of") class ScriptStatementVisitor extends GrammarBaseVisitor<Node> {

    private final String sourceFile;

    @Override
    public Node visitScriptStatement(final GrammarParser.ScriptStatementContext ctx) {
        if (ctx.statement() != null) {
            return ctx.statement().accept(StatementVisitor.of(sourceFile));
        } else if (ctx.varDef() != null) {
            return ctx.varDef().accept(FieldVisitor.of(sourceFile));
        }

        throw new RuntimeException("Unimplemented statement: " + ctx.getText());
    }
}
