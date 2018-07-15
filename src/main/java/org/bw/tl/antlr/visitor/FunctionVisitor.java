package org.bw.tl.antlr.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bw.tl.antlr.GrammarBaseVisitor;
import org.bw.tl.antlr.GrammarParser;
import org.bw.tl.antlr.ast.Block;
import org.bw.tl.antlr.ast.Function;
import org.bw.tl.antlr.ast.QualifiedName;

@RequiredArgsConstructor(staticName = "of")
public class FunctionVisitor extends GrammarBaseVisitor<Function> {

    private final @Getter String sourceFile;

    @Override
    public Function visitFunctionDef(final GrammarParser.FunctionDefContext ctx) {
        final QualifiedName type = ctx.type() != null ? QualifiedName.of(ctx.type().getText()) :
                QualifiedName.of("void");

        final Block block = ctx.block().accept(BlockVisitor.of(sourceFile));
        final String name = ctx.IDENTIFIER().getText();

        QualifiedName[] paramTypes = new QualifiedName[0];
        String[] paramNames = new String[0];

        if (ctx.functionParamDefs() != null) {
            paramTypes = ctx.functionParamDefs().functionParam().stream()
                    .map(p -> QualifiedName.of(p.type().getText())).toArray(QualifiedName[]::new);
            paramNames = ctx.functionParamDefs().functionParam().stream()
                    .map(p -> p.IDENTIFIER().getText()).toArray(String[]::new);
        }

        final Function function = new Function(paramTypes, paramNames, name, block, type);

        if (ctx.modifierList() != null && ctx.modifierList().modifier() != null) {
            for (final GrammarParser.ModifierContext modCtx : ctx.modifierList().modifier()) {
                function.addModifiers(modCtx.accept(new ModifierVisitor()));
            }
        }

        block.setParent(function);

        function.setText(ctx.getText());
        function.setFile(sourceFile);
        function.setLineNumber(ctx.start.getLine());

        return function;
    }
}
