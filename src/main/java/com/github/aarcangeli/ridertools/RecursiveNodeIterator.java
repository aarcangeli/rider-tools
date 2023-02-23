package com.github.aarcangeli.ridertools;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecursiveNodeIterator {
    private final List<LeafElement> nodes;
    private int index = 0;

    public RecursiveNodeIterator(@NotNull ASTNode rootElement) {
        nodes = getAllNodes(rootElement);
        // Add EOF
        nodes.add(null);
    }

    public void advanceElement() {
        assert !eof();
        index++;
    }

    public LeafElement getPreviousElement() {
        assert index > 0;
        return nodes.get(index - 1);
    }

    public LeafElement getCurrentElement() {
        return index < nodes.size() ? nodes.get(index) : null;
    }

    public IElementType currentElementType() {
        return index < nodes.size() ? nodes.get(index).getElementType() : null;
    }

    public IElementType nextElementType() {
        return index + 1 < nodes.size() ? nodes.get(index + 1).getElementType() : null;
    }

    public IElementType lookAhead(int count) {
        assert !eof();
        return index + count < nodes.size() ? nodes.get(index + count).getElementType() : null;
    }

    public boolean eof() {
        return index == nodes.size() - 1;
    }


    private static List<LeafElement> getAllNodes(ASTNode node) {
        List<LeafElement> result = new ArrayList<>();
        getNodesRecursive(result, node);
        return result;
    }

    private static void getNodesRecursive(@NotNull List<LeafElement> result, @NotNull ASTNode node) {
        for (ASTNode child : node.getChildren(null)) {
            if (child instanceof PsiWhiteSpace || child instanceof PsiComment) {
                // Skip whitespace
            } else if (child instanceof LeafElement) {
                result.add((LeafElement) child);
            } else {
                getNodesRecursive(result, child);
            }
        }
    }
}
