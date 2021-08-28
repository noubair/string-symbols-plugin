package org.jetbrains.plugins.template.services

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.layout.panel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


class AskQuestionAction : AnAction()  {
    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel: CaretModel = editor.getCaretModel()
        val selectedText = caretModel.currentCaret.selectedText
//        BrowserUtil.browse("https://www.google.com/search?q=%s".format(selectedText))
        val myPopup = DslPopup()
        myPopup.showPopup()

    }
}


class MyPopup {
    val content = JPanel().apply {
        val label = JLabel("My Label")
        add(label)

        val bt = JButton("ok")
        bt.addActionListener {
            println("clicked ok")
            dispose()
        }
        add(bt)
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