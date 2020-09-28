package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.*;

import java.util.ArrayList;
import java.util.LinkedList;

@RequiredArgsConstructor(staticName = "of")
public class FileVisitor extends GrammarBaseVisitor<Clazz> {

    private final @Getter
    String sourceFile;

    @Override
    public Clazz visitFile(final GrammarParser.FileContext ctx) {
        QualifiedName packageName = new QualifiedName("default");
        final ArrayList<ScheduleBlock> scheduleBlocks = new ArrayList<>();
        final ArrayList<QualifiedName> imports = new ArrayList<>();
        final ArrayList<Field> fields = new ArrayList<>();
        final ArrayList<Function> functions = new ArrayList<>();

        if (ctx.packageDef() != null) {
            packageName = ctx.packageDef().fqn().accept(FQNVisitor.of(sourceFile));
        }

        if (ctx.imp() != null) {
            for (final GrammarParser.ImpContext impCtx : ctx.imp()) {
                final QualifiedName fileImport = impCtx.fqn().accept(FQNVisitor.of(sourceFile));
                imports.add(fileImport);
            }
        }

        if (ctx.topLevelStatement() != null) {
            for (final GrammarParser.TopLevelStatementContext tlCtx : ctx.topLevelStatement()) {
                if (tlCtx.functionDef() != null) {
                    functions.add(tlCtx.functionDef().accept(FunctionVisitor.of(sourceFile)));
                } else if (tlCtx.varDef() != null) {
                    fields.add(tlCtx.varDef().accept(FieldVisitor.of(sourceFile)));
                } else if (tlCtx.schedule() != null) {
                    scheduleBlocks.add(tlCtx.schedule().accept(ScheduleVisitor.of(sourceFile)));
                }
            }
        }

        // add schedule to class

        return new Clazz(packageName, imports, new LinkedList<>(), fields, functions, scheduleBlocks, sourceFile);
    }
}
