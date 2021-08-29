package org.jetbrains.plugins.template.services

import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.impl.cache.CacheManager
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.ui.layout.panel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

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
        if (element is  PsiJavaTokenImpl && "STRING_LITERAL".equals(element.elementType.toString())){ //TODO: not a reliable condition?
            handleStringLiteral(e, element, editor)
        }

    }

    private fun handleStringLiteral(
        e: AnActionEvent,
        element: PsiJavaTokenImpl,
        editor: Editor
    ) {
        val myManager = PsiManagerEx.getInstanceEx(e.project);
        val word = element.text.replace("\"", "").replace("'", "")
        val methodFiles: List<PsiJavaFile> = CacheManager.getInstance(myManager.getProject()).getFilesWithWord(
            word, UsageSearchContext.IN_CODE,
            GlobalSearchScope.projectScope(myManager.getProject()),
            true
        ).map { psiFile -> psiFile as PsiJavaFile }


        //TODO: handle the possibility of multiple classes
        val candidateMethods = arrayListOf<PsiElement>()
        methodFiles.forEach { javaFile ->
            javaFile.classes.forEach { aClass ->
                aClass.methods.forEach { aMethod ->
                    candidateMethods.add(
                        aMethod
                    )
                }
            }
        }

        NavigationUtil.getPsiElementPopup(
            candidateMethods.toTypedArray(),
            DefaultPsiElementCellRenderer(),
            "Choose Definition"
        )
            .showInBestPositionFor(editor)
    }

    fun other(e: AnActionEvent){
        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel: CaretModel = editor.caretModel
        caretModel.currentCaret.selectWordAtCaret(true)
        val word = caretModel.currentCaret.selectedText
        val virtualFile = FileEditorManager.getInstance(e.project!!).selectedFiles.get(0)
        val file = PsiManager.getInstance(e.project!!).findFile(virtualFile)
        val element = file!!.findElementAt(editor.caretModel.offset)
        val usages = ReferencesSearch.search(element!!.parent!!).findAll()
//        val myPopup = MyPopup(word!!)
//        myPopup.show()
    }
}