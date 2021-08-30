package org.jetbrains.plugins.template.services

import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.cache.CacheManager
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.UsageSearchContext
import org.jetbrains.uast.test.env.findUElementByTextFromPsi

class StringSymbolsAction : AnAction()  {
    override fun actionPerformed(e: AnActionEvent) {
        navigateToMethodFromString(e)
    }

    fun navigateToMethodFromString(e: AnActionEvent) {
        //TODO: if many results, filter by candidate
        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val virtualFile = FileEditorManager.getInstance(e.project!!).selectedFiles.get(0)
        val file = PsiManager.getInstance(e.project!!)
            .findFile(virtualFile) //TODO: use anActionEvent.getData(CommonDataKeys.PSI_FILE);
        val element = file!!.findElementAt(editor.caretModel.offset)!!
        if (element is  PsiJavaTokenImpl && JavaTokenType.STRING_LITERAL.toString() == element.elementType.toString()){ //TODO: not a reliable condition?
            handleStringLiteral(e, element, editor)
        } else if (element is  PsiIdentifierImpl && JavaTokenType.IDENTIFIER.toString() ==  element.elementType.toString()){
            handleMethodDefinition(element, e, editor)
        }

    }

    private fun handleMethodDefinition(
        element: PsiIdentifierImpl,
        e: AnActionEvent,
        editor: Editor
    ) {
        val methodName = element.text
        val quotedMethodName = "\"" + methodName + "\""
        val methodFiles: List<PsiJavaFile> = CacheManager.getInstance(e.project).getFilesWithWord(
            methodName, UsageSearchContext.IN_STRINGS,
            GlobalSearchScope.projectScope(e.project!!),
            true
        ).map { psiFile -> psiFile as PsiJavaFile }
        val candidatePsiMethodInvocations = arrayListOf<PsiElement?>()
        methodFiles.forEach { javaFile ->
            run {
                var index = javaFile.text.indexOf(quotedMethodName);
                while (index >= 0) {
                    candidatePsiMethodInvocations.add(javaFile.findElementAt(index))
                    index = javaFile.text.indexOf(quotedMethodName, index + 1)
                }
            }
        }
        NavigationUtil.getPsiElementPopup(
            candidatePsiMethodInvocations.toTypedArray(),
            DefaultPsiElementCellRenderer(),
            "Choose Definition"
        ).showInBestPositionFor(editor)
    }

    private fun handleStringLiteral(
        e: AnActionEvent,
        element: PsiJavaTokenImpl,
        editor: Editor
    ) {
        val methodName = element.text.replace("\"", "").replace("'", "")
        val methodFiles: List<PsiJavaFile> = CacheManager.getInstance(e.project).getFilesWithWord(
            methodName, UsageSearchContext.IN_CODE,
            GlobalSearchScope.projectScope(e.project!!),
            true
        ).map { psiFile -> psiFile as PsiJavaFile }
        val candidatePsiMethods = arrayListOf<PsiElement>()
        methodFiles.forEach { javaFile ->
            javaFile.classes.forEach { aClass ->
                aClass.methods
                    .filter {
                        aMethod ->
                        aMethod.name == methodName
                    }
                    .forEach { aMethod ->
                    candidatePsiMethods.add(
                        aMethod
                    )
                }
            }
        }
        NavigationUtil.getPsiElementPopup(
            candidatePsiMethods.toTypedArray(),
            DefaultPsiElementCellRenderer(),
            "Choose Definition"
        ).showInBestPositionFor(editor)
    }
}