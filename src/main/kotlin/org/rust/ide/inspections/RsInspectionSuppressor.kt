package org.rust.ide.inspections

import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.SuppressionUtil
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsItemElement
import org.rust.lang.core.psi.util.ancestors
import org.rust.lang.core.psi.util.parentOfType

class RsInspectionSuppressor : InspectionSuppressor {
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<out SuppressQuickFix> = arrayOf(
        SuppressInspectionFix(toolId),
        SuppressInspectionFix(SuppressionUtil.ALL)
    )

    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean =
        element.ancestors.filterIsInstance<RsItemElement>()
            .any { isSuppressedByComment(it, toolId) }

    private fun isSuppressedByComment(element: PsiElement, toolId: String): Boolean {
        val comment = PsiTreeUtil.skipSiblingsBackward(element, PsiWhiteSpace::class.java) as? PsiComment
            ?: return false
        val matcher = SuppressionUtil.SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(comment.text)
        return matcher.matches() && SuppressionUtil.isInspectionToolIdMentioned(matcher.group(1), toolId)
    }

    private class SuppressInspectionFix(
        ID: String
    ) : AbstractBatchSuppressByNoInspectionCommentFix(ID, /* replaceOthers = */ ID == SuppressionUtil.ALL) {

        init {
            text = if (ID == SuppressionUtil.ALL) "Suppress all inspections for item" else "Suppress for item"
        }

        override fun getContainer(context: PsiElement?): PsiElement? {
            if (context == null) return null
            return context.parentOfType<RsItemElement>(strict = false)
        }
    }
}

