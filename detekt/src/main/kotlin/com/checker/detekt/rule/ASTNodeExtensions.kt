@file:Suppress("unused")

package com.checker.detekt.rule

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE

internal fun ASTNode.prevLeaf(): ASTNode? {
    var n = prevLeafAny()
    while (n != null && n.textLength == 0) {
        n = n.prevLeafAny()
    }
    return n
}

private fun ASTNode.prevLeafAny(): ASTNode? =
    if (treePrev != null) treePrev.lastChildLeafOrSelf() else treeParent?.prevLeafAny()

internal fun ASTNode.lastChildLeafOrSelf(): ASTNode {
    var n = this
    while (n.lastChildNode != null) {
        n = n.lastChildNode
    }
    return n
}

internal fun ASTNode.lineIndent(): String {
    var leaf = prevLeaf()
    while (leaf != null) {
        if (leaf.elementType == WHITE_SPACE && leaf.textContains('\n')) {
            return leaf.text.substring(leaf.text.lastIndexOf('\n') + 1)
        }
        leaf = leaf.prevLeaf()
    }
    return ""
}