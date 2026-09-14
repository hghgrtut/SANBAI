@file:Suppress("unused")

package com.checker.detekt.rule

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.lexer.KtTokens.REGULAR_STRING_PART
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import kotlin.reflect.KClass

internal fun ASTNode.nextLeaf(
    includeEmpty: Boolean = false,
    skipSubtree: Boolean = false
): ASTNode? {
    var n = if (skipSubtree) lastChildLeafOrSelf().nextLeafAny() else nextLeafAny()
    if (!includeEmpty) {
        while (n != null && n.textLength == 0) {
            n = n.nextLeafAny()
        }
    }
    return n
}

internal fun ASTNode.nextLeaf(p: (ASTNode) -> Boolean): ASTNode? {
    var n = nextLeafAny()
    while (n != null && !p(n)) {
        n = n.nextLeafAny()
    }
    return n
}

private fun ASTNode.nextLeafAny(): ASTNode? {
    var n = this
    if (n.firstChildNode != null) {
        do {
            n = n.firstChildNode
        } while (n.firstChildNode != null)
        return n
    }
    return n.nextLeafStrict()
}

private fun ASTNode.nextLeafStrict(): ASTNode? {
    val nextSibling: ASTNode? = treeNext
    if (nextSibling != null) {
        return nextSibling.firstChildLeafOrSelf()
    }
    return treeParent?.nextLeafStrict()
}

internal fun ASTNode.firstChildLeafOrSelf(): ASTNode {
    var n = this
    if (n.firstChildNode != null) {
        do {
            n = n.firstChildNode
        } while (n.firstChildNode != null)
        return n
    }
    return n
}

internal fun ASTNode.prevLeaf(includeEmpty: Boolean = false): ASTNode? {
    var n = prevLeafAny()
    if (!includeEmpty) {
        while (n != null && n.textLength == 0) {
            n = n.prevLeafAny()
        }
    }
    return n
}

internal fun ASTNode.prevLeaf(p: (ASTNode) -> Boolean): ASTNode? {
    var n = prevLeafAny()
    while (n != null && !p(n)) {
        n = n.prevLeafAny()
    }
    return n
}

private fun ASTNode.prevLeafAny(): ASTNode? {
    val prevSibling = treePrev
    if (prevSibling != null) {
        return treePrev.lastChildLeafOrSelf()
    }
    return treeParent?.prevLeafAny()
}

internal fun ASTNode.lastChildLeafOrSelf(): ASTNode {
    var n = this
    if (n.lastChildNode != null) {
        do {
            n = n.lastChildNode
        } while (n.lastChildNode != null)
        return n
    }
    return n
}

internal fun ASTNode.prevCodeLeaf(includeEmpty: Boolean = false): ASTNode? {
    var n = prevLeaf(includeEmpty)
    while (n != null && (n.elementType == WHITE_SPACE || n.isPartOfComment())) {
        n = n.prevLeaf(includeEmpty)
    }
    return n
}

internal fun ASTNode.nextCodeLeaf(
    includeEmpty: Boolean = false,
    skipSubtree: Boolean = false
): ASTNode? {
    var n = nextLeaf(includeEmpty, skipSubtree)
    while (n != null && (n.elementType == WHITE_SPACE || n.isPartOfComment())) {
        n = n.nextLeaf(includeEmpty, skipSubtree)
    }
    return n
}

internal fun ASTNode.prevCodeSibling(): ASTNode? =
    prevSibling { it.elementType != WHITE_SPACE && !it.isPartOfComment() }

internal inline fun ASTNode.prevSibling(p: (ASTNode) -> Boolean): ASTNode? {
    var n = treePrev
    while (n != null) {
        if (p(n)) {
            return n
        }
        n = n.treePrev
    }
    return null
}

internal fun ASTNode.nextCodeSibling(): ASTNode? =
    nextSibling { it.elementType != WHITE_SPACE && !it.isPartOfComment() }

internal inline fun ASTNode.nextSibling(p: (ASTNode) -> Boolean): ASTNode? {
    var n = treeNext
    while (n != null) {
        if (p(n)) {
            return n
        }
        n = n.treeNext
    }
    return null
}

internal fun ASTNode.parent(
    elementType: IElementType,
    strict: Boolean = true
): ASTNode? {
    var n: ASTNode? = if (strict) treeParent else this
    while (n != null) {
        if (n.elementType == elementType) {
            return n
        }
        n = n.treeParent
    }
    return null
}

internal fun ASTNode.parent(
    p: (ASTNode) -> Boolean,
    strict: Boolean = true
): ASTNode? {
    var n: ASTNode? = if (strict) treeParent else this
    while (n != null) {
        if (p(n)) {
            return n
        }
        n = n.treeParent
    }
    return null
}

internal fun ASTNode.isPartOf(elementType: IElementType) = parent(elementType, strict = false) != null

internal fun ASTNode.isPartOf(klass: KClass<out PsiElement>): Boolean {
    var n: ASTNode? = this
    while (n != null) {
        if (klass.java.isInstance(n.psi)) {
            return true
        }
        n = n.treeParent
    }
    return false
}

internal fun ASTNode.isPartOfString() = parent(KtNodeTypes.STRING_TEMPLATE, strict = false) != null

internal fun ASTNode?.isWhiteSpace() = this != null && elementType == WHITE_SPACE

internal fun ASTNode?.isWhiteSpaceWithNewline() = this != null && elementType == WHITE_SPACE && textContains('\n')

internal fun ASTNode?.isWhiteSpaceWithoutNewline() = this != null && elementType == WHITE_SPACE && !textContains('\n')

internal fun ASTNode.isLeaf() = firstChildNode == null

internal fun ASTNode.isPartOfComment() = parent({ it.psi is PsiComment }, strict = false) != null

internal fun ASTNode.children() = generateSequence(firstChildNode) { node -> node.treeNext }

internal fun LeafElement.upsertWhitespaceBeforeMe(text: String): LeafElement {
    val s = treePrev
    return if (s != null && s.elementType == WHITE_SPACE) {
        (s.psi as LeafElement).rawReplaceWithText(text)
    } else {
        PsiWhiteSpaceImpl(text).also { w ->
            (psi as LeafElement).rawInsertBeforeMe(w)
        }
    }
}

internal fun LeafElement.upsertWhitespaceAfterMe(text: String): LeafElement {
    val s = treeNext
    return if (s != null && s.elementType == WHITE_SPACE) {
        (s.psi as LeafElement).rawReplaceWithText(text)
    } else {
        PsiWhiteSpaceImpl(text).also { w ->
            (psi as LeafElement).rawInsertAfterMe(w)
        }
    }
}

internal fun ASTNode.visit(enter: (node: ASTNode) -> Unit) {
    enter(this)
    getChildren(null).forEach { it.visit(enter) }
}

internal fun ASTNode.visit(
    enter: (node: ASTNode) -> Unit,
    exit: (node: ASTNode) -> Unit
) {
    enter(this)
    getChildren(null).forEach { it.visit(enter, exit) }
    exit(this)
}

internal fun ASTNode.lineNumber(): Int? =
    psi.containingFile?.viewProvider?.document?.getLineNumber(startOffset)?.let { it + 1 }

internal val ASTNode.column: Int
    get() {
        var leaf = prevLeaf()
        var offsetToTheLeft = 0
        while (leaf != null) {
            if (leaf.elementType.let { it == WHITE_SPACE || it == REGULAR_STRING_PART } && leaf.textContains('\n')) {
                offsetToTheLeft += leaf.textLength - 1 - leaf.text.lastIndexOf('\n')
                break
            }
            offsetToTheLeft += leaf.textLength
            leaf = leaf.prevLeaf()
        }
        return offsetToTheLeft + 1
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
