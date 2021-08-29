package org.jetbrains.plugins.template.services

import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.SearchInBackgroundOption
import com.intellij.find.impl.FindInProjectUtil
import com.intellij.find.impl.FindInProjectUtil.StringUsageTarget
import com.intellij.find.impl.FindManagerImpl
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.util.Factory
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.search.SearchScope
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewBundle
import com.intellij.usages.*
import com.intellij.usages.UsageViewManager.UsageViewStateListener
import com.intellij.usages.impl.UsageViewEx
import com.intellij.usages.impl.UsageViewManagerImpl
import com.intellij.util.Processor
import java.util.concurrent.atomic.AtomicReference


internal class StringSymbolsActionTest {


}






