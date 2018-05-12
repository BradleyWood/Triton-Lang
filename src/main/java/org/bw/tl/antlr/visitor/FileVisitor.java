package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Field;
import org.bw.tl.antlr.ast.File;
import org.bw.tl.antlr.ast.Function;
import org.bw.tl.antlr.ast.QualifiedName;

import java.util.ArrayList;

@RequiredArgsConstructor(staticName = "of")
public class FileVisitor extends GrammarBaseVisitor<File> {

    private final @Getter String sourceFile;

    @Override
    public File visitFile(final GrammarParser.FileContext ctx) {
        QualifiedName packageName = new QualifiedName(new String[]{"default"});
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
                }
            }
        }

        return new File(packageName, imports, fields, functions, sourceFile);
    }
}
