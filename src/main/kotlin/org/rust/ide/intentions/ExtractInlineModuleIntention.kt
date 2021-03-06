package org.rust.ide.intentions

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsModDeclItem
import org.rust.lang.core.psi.RsModItem
import org.rust.lang.core.psi.RsPsiFactory
import org.rust.lang.core.psi.impl.mixin.getOrCreateModuleFile
import org.rust.lang.core.psi.util.parentOfType

//TODO: make context more precise here
class ExtractInlineModuleIntention : RsElementBaseIntentionAction<RsModItem>() {
    override fun getFamilyName() = "Extract inline module structure"
    override fun getText() = "Extract inline module"

    override fun findApplicableContext(project: Project, editor: Editor, element: PsiElement): RsModItem? {
        val mod = element.parentOfType<RsModItem>() ?: return null
        if (mod.`super`?.ownsDirectory != true) return null
        return mod

    }

    override fun invoke(project: Project, editor: Editor, ctx: RsModItem) {
        val modName = ctx.name ?: return
        var decl = RsPsiFactory(project).createModDeclItem(modName)
        decl = ctx.parent?.addBefore(decl, ctx) as? RsModDeclItem ?: return
        val modFile = decl.getOrCreateModuleFile() ?: return

        val startElement = ctx.lbrace.nextSibling ?: return
        val endElement = ctx.rbrace?.prevSibling ?: return

        modFile.addRange(startElement, endElement)
        ReformatCodeProcessor(project, modFile, null, false).run()

        ctx.delete()
    }
}
