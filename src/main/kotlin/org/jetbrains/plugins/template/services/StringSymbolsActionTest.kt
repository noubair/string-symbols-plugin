package org.jetbrains.plugins.template.services

import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.findInProject.FindInProjectManager
import com.intellij.find.impl.FindInProjectUtil
import com.intellij.find.impl.FindInProjectUtil.StringUsageTarget
import com.intellij.find.impl.FindManagerImpl
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.usageView.UsageInfo
import com.intellij.usages.*
import com.intellij.util.Processor


internal class StringSymbolsActionTest {

    @org.junit.jupiter.api.Test
    fun t1(e: AnActionEvent) {
        //        val selectedText = caretModel.currentCaret.selectedText.toString()
//        val file = SearchService.getInstance().searchWord(e.project!!, "updateIndicator").toString()
//        val file = PsiSearchHelper.getInstance(e.project!!).findFilesWithPlainTextWords(selectedText).get(0).name
//        val file = PsiSearchHelper.getInstance(e.project!!).processAllFilesWithWord()
//        val myPopup = MyPopup(file)
//        myPopup.show()



//        val virtualFile = FileEditorManager.getInstance(e.project!!).selectedFiles.get(0)
//        var file = PsiManager.getInstance(e.project!!).findFile(virtualFile)
//        var element = file.findElementAt( e.getRequiredData(CommonDataKeys.EDITOR).getCaretModel().getOffset())
//        //search(element!!, GlobalSearchScope.projectScope(e.project!!)).findAll()
//        element
    }

    public fun t2(e: AnActionEvent){
        var findModel = FindModel()
        findModel.setStringToFind("update")
        startFindInProject(findModel, e)

//        FindInProjectManager.getInstance(e.project).findInPath()
    }

    fun startFindInProject(findModel: FindModel, e: AnActionEvent) {
        var myProject = e.project!!;
        if (findModel.directoryName != null && FindInProjectUtil.getDirectory(findModel) == null) {
            return
        }
        val manager = UsageViewManager.getInstance(myProject) ?: return
        val findManager = FindManager.getInstance(myProject)
        findManager.findInProjectModel.copyFrom(findModel)
        val findModelCopy = findModel.clone()
        val presentation = FindInProjectUtil.setupViewPresentation(findModelCopy)
        val processPresentation = FindInProjectUtil.setupProcessPresentation(myProject, presentation)
        val usageTarget: ConfigurableUsageTarget = StringUsageTarget(myProject, findModel)
        (FindManager.getInstance(myProject) as FindManagerImpl).findUsagesManager.addToHistory(usageTarget)

        manager.searchAndShowUsages(
            arrayOf<UsageTarget>(usageTarget),
            {
                UsageSearcher { processor: Processor<in Usage?> ->
                    var myIsFindInProgress = true
                    try {
                        val consumer =
                            Processor { info: UsageInfo? ->
                                val usage = UsageInfo2UsageAdapter.CONVERTER.`fun`(info)
                                usage.presentation.icon // cache icon
                                processor.process(usage)
                            }
                        FindInProjectUtil.findUsages(findModelCopy, myProject, consumer, processPresentation)
                    } finally {
                        myIsFindInProgress = false
                    }
                }
            },
            processPresentation,
            presentation,
            null
        )
    }
}






