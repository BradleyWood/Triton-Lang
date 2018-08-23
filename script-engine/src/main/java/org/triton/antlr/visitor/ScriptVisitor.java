package org.triton.antlr.visitor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Node;
import org.bw.tl.antlr.ast.QualifiedName;
import org.triton.antlr.ast.Script;

import java.util.LinkedList;

@EqualsAndHashCode(callSuper = true)
public @Data(staticConstructor = "of") class ScriptVisitor extends GrammarBaseVisitor<Script> {

    private final String sourceFile;

    @Override
    public Script visitScript(final GrammarParser.ScriptContext ctx) {
        final LinkedList<QualifiedName> imports = new LinkedList<>();
        final LinkedList<Node> statements = new LinkedList<>();

        if (ctx.imp() != null) {
            ctx.imp().forEach(i -> imports.add(QualifiedName.of(i.getText())));
        }

        if (ctx.scriptStatement() != null) {
            for (final GrammarParser.ScriptStatementContext scriptStatementContext : ctx.scriptStatement()) {
                statements.add(scriptStatementContext.accept(ScriptStatementVisitor.of(sourceFile)));
            }
        }

        return new Script(imports, statements, sourceFile);
    }

}
