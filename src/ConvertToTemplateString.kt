import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory

/**
 * Created by Juan on 22/06/2016
 */
internal class ConvertToTemplateString : AnAction("Convert to template string") {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        val caret = event.getData(PlatformDataKeys.CARET)
        val editor = event.getData(PlatformDataKeys.EDITOR)
        var document: Document? = null
        if (editor != null) {
            document = editor.document
        }
        var psiFile: PsiFile? = null
        if (project != null && document != null) {
            psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
        }

        var psiElement: PsiElement? = null
        if (psiFile != null && caret != null) {
            psiElement = psiFile.findElementAt(caret.offset)
        }

        if (psiElement != null) {
            val newStyledComponentTag = "<fkt/>"

            val styledComponent = PsiFileFactory.getInstance(project!!).createFileFromText(newStyledComponentTag, psiFile!!)

            var runnable: Runnable? = null
            if (styledComponent != null) {
                val finalPsiElement = psiElement
                runnable = Runnable { finalPsiElement.replace(styledComponent) }
            }
            if (runnable != null) {
                WriteCommandAction.runWriteCommandAction(project, runnable)
            }

            val htmlElement = psiElement.text
            val styledComponentDefnition = "const fkt = styled.$htmlElement`\n\n`;"

            val styledComponentDefinitionPsi = PsiFileFactory.getInstance(project!!).createFileFromText(styledComponentDefnition, psiFile!!)

            if (styledComponentDefinitionPsi != null) {
                val lastElement = psiFile.node.lastChildNode.psi
                runnable = Runnable {
                    psiFile!!.addAfter(styledComponentDefinitionPsi, lastElement)
                }
            }
            if (runnable != null) {
                WriteCommandAction.runWriteCommandAction(project, runnable)
            }
        }
    }
}