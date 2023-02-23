package com.github.aarcangeli.ridertools;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.rider.cpp.fileType.lexer.CppTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class FoldingDescriptorFinder {
    private final List<FoldingDescriptor> result = new ArrayList<>();
    private final ASTNode rootNode;

    public FoldingDescriptorFinder(@NotNull ASTNode rootNode) {
        this.rootNode = rootNode;
    }

    public FoldingDescriptor[] getFoldingDescriptors() {
        return result.toArray(FoldingDescriptor[]::new);
    }

    public void findElements() {
        result.clear();

        RecursiveNodeIterator iterator = new RecursiveNodeIterator(rootNode);
        while (!iterator.eof()) {
            if (iterator.currentElementType() == CppTokenTypes.LBRACE) {
                scanScope(iterator);
                continue;
            }
            iterator.advanceElement();
        }
    }

    private void scanScope(RecursiveNodeIterator iterator) {
        assert iterator.currentElementType() == CppTokenTypes.LBRACE;
        iterator.advanceElement();

        int currentBlockStart = -1;

        while (!iterator.eof()) {
            if (iterator.currentElementType() == CppTokenTypes.LBRACE) {
                scanScope(iterator);
                continue;
            }

            if (iterator.currentElementType() == CppTokenTypes.RBRACE) {
                if (currentBlockStart != -1) {
                    int end = iterator.getPreviousElement().getTextRange().getEndOffset();
                    result.add(new FoldingDescriptor(rootNode, new TextRange(currentBlockStart, end)));
                }
                iterator.advanceElement();
                break;
            }

            if (iterator.currentElementType() == CppTokenTypes.KEYWORD && iterator.nextElementType() == CppTokenTypes.COLON) {
                String text = iterator.getCurrentElement().getText();
                if (text.equals("public") || text.equals("private") || text.equals("protected")) {
                    // Create block if necessary
                    if (currentBlockStart != -1) {
                        int end = iterator.getPreviousElement().getTextRange().getEndOffset();
                        result.add(new FoldingDescriptor(rootNode, new TextRange(currentBlockStart, end)));
                    }

                    iterator.advanceElement();
                    iterator.advanceElement();
                    currentBlockStart = iterator.getPreviousElement().getTextRange().getEndOffset();
                    continue;
                }
            }

            iterator.advanceElement();
        }
    }

    @Nullable
    private static IElementType findByDebugName(String debugName) {
        IElementType[] filtered = IElementType.enumerate(type -> type.getDebugName().equals(debugName));
        return filtered.length > 0 ? filtered[0] : null;
    }
}
