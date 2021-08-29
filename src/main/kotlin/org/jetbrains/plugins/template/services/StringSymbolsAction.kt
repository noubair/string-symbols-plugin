package org.jetbrains.plugins.template.services

import com.intellij.find.findUsages.CustomUsageSearcher
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.find.usages.SymbolTextSearcher
import com.intellij.ide.BrowserUtil
import com.intellij.model.search.SearchService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiFileEx
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.impl.cache.CacheManager
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiFileReferenceHelper
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.ReferenceSearcher
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.search.searches.ReferencesSearch.search
import com.intellij.psi.util.findDescendantOfType
import com.intellij.ui.layout.panel
import com.intellij.usages.Usage
import org.jetbrains.uast.test.env.findUElementByTextFromPsi
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
//import com.intellij.psi.PsiJavaFile


class StringSymbolsAction : AnAction()  {
    override fun actionPerformed(e: AnActionEvent) {
        navigateToMethodFromString(e)
    }

    fun navigateToMethodFromString(e: AnActionEvent){
        //TODO: if many results, filter by candidate
        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val virtualFile = FileEditorManager.getInstance(e.project!!).selectedFiles.get(0)
        val file = PsiManager.getInstance(e.project!!).findFile(virtualFile) //TODO: use anActionEvent.getData(CommonDataKeys.PSI_FILE);
        val element = file!!.findElementAt(editor.caretModel.offset)!!
        val myManager = PsiManagerEx.getInstanceEx(e.project);
        val word = element.text.replace("\"", "").replace("'", "")
        val methodFile: PsiJavaFile = CacheManager.getInstance(myManager.getProject()).getFilesWithWord(
            word, UsageSearchContext.IN_CODE,
            GlobalSearchScope.projectScope(myManager.getProject()),
            true
        ).get(0) as PsiJavaFile

        //TODO: handle the possibility of multiple classes
        methodFile.findDescendantOfType<PsiElement>()
        var method = methodFile.classes.get(0).findMethodsByName(word).get(0)
        FileEditorManager.getInstance(e.project!!).openFile(methodFile.virtualFile, true)
        val menuContent =  methodFile.toString();
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
        val myPopup = MyPopup(word!!)
        myPopup.show()
    }
}


class MyPopup(labelString: String) {
    val content = JPanel().apply {
        val label = JLabel(labelString)
        add(label)

        val bt = JButton("ok")
        bt.addActionListener {
            println("clicked ok")
            dispose()
        }
//        add(bt)
    }
    val popup = JBPopupFactory.getInstance()
        .createComponentPopupBuilder(content, null)
        .createPopup()

    fun dispose() {
        popup.dispose()
    }

    fun show() {
        popup.showInFocusCenter()

    }
}

class DslPopup {
    fun createPanel(): DialogPanel {
        return panel {
            noteRow("Login to get notified when the submitted\nexceptions are fixed.")
            row("Username:") { JTextField() }
            row("Password:") { textField({ "XXXXX" }, { println(it) }) }
            row {
                checkBox("AAA")
                row {
                    label("BBB")
                    checkBox("CCC")
                }
                right {
                    link("Forgot password?") { /* custom action */ }
                }
            }
            noteRow("""Do not have an account? <a href="https://account.jetbrains.com/login">Sign Up</a>""")
        }
    }

    fun showPopup() {
        JBPopupFactory.getInstance()
            .createComponentPopupBuilder(createPanel(), null)
            .createPopup()
            .showInFocusCenter()
    }
}