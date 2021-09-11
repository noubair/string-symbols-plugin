package com.github.noubair.stringsymbolsplugin.services

import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.ide.util.PsiElementListCellRenderer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.Iconable
import com.intellij.psi.*
import com.intellij.psi.impl.cache.CacheManager
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.impl.source.tree.PsiPlainTextImpl
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl
import com.intellij.psi.presentation.java.SymbolPresentationUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.util.PsiTreeUtil

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
        if (element is PsiJavaTokenImpl && JavaTokenType.STRING_LITERAL.toString() == element.elementType.toString()) { //TODO: not a reliable condition?
            handleStringLiteral(e, element, editor)
        } else if (element is PsiIdentifierImpl && JavaTokenType.IDENTIFIER.toString() == element.elementType.toString()) {
            handleMethodDefinition(element, e, editor)
        } else if (element is PsiPlainTextImpl) {
            handlePlainText(editor, element)
        }

    }

    private fun handlePlainText(
        editor: Editor,
        element: PsiPlainTextImpl
    ) {
        val offset = editor.caretModel.offset
        var classNameCandidate = ""
        var c = offset
        while (isNotSpecialChar(element.text[c])) {
            classNameCandidate = element.text[c] + classNameCandidate
            c--
        }
        c = offset +1
        while (isNotSpecialChar(element.text[c])) {
            classNameCandidate += element.text[c]
            c++
        }
        val camelCaseClassName = classNameCandidate.split(" ").stream()
            .reduce { w1: String, w2: String -> w1.replaceFirstChar(Char::uppercase) + w2.replaceFirstChar(Char::uppercase) }
            .get()
        val filesWithClassName: List<PsiElement> = CacheManager.getInstance(element.project).getFilesWithWord(
            camelCaseClassName, UsageSearchContext.IN_CODE,
            GlobalSearchScope.projectScope(element.project),
            true
        ).map { psiFile ->
            (psiFile as PsiJavaFile).classes.filter { classNameCandidate ->
                classNameCandidate.name.equals(camelCaseClassName)
            }.get(0)
        }


        NavigationUtil.getPsiElementPopup(
            filesWithClassName.toTypedArray(),
            DefaultPsiElementCellRenderer(),
            "Choose Class Definition"
        ).showInBestPositionFor(editor)

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
                    candidatePsiMethodInvocations.add(javaFile.findElementAt(index)!!)
                    index = javaFile.text.indexOf(quotedMethodName, index + 1)
                }
            }
        }
        NavigationUtil.getPsiElementPopup(
            candidatePsiMethodInvocations.toTypedArray(),
            MethodStringInvocationsRenderer(),
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


    fun isNotSpecialChar(char: Char): Boolean {
        return char.isLetter() || char.isDigit() || char.isWhitespace()
    }
}


class MethodStringInvocationsRenderer : PsiElementListCellRenderer<PsiElement?>() {
    override fun getIconFlags(): Int {
        return Iconable.ICON_FLAG_VISIBILITY
    }

    override fun getElementText(element: PsiElement?): String {
        return SymbolPresentationUtil.getSymbolPresentableText(element!!)
    }

    public override fun getContainerText(element: PsiElement?, name: String): String? {
        val innerClass: PsiClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)!!;
        return "(in " + (innerClass.containingFile as PsiJavaFileImpl).packageName + "." + innerClass.name + ")";
    }

    companion object {
        fun getFirstParentMethod() : Int = 1
    }
}