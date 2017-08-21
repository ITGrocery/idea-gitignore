/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 hsz Jakub Chrzanowski <jakub@hsz.mobi>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package mobi.hsz.idea.gitignore.daemon

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.WeakKeyWeakValueHashMap
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.command.AppendFileCommandAction
import mobi.hsz.idea.gitignore.file.type.kind.GitFileType
import mobi.hsz.idea.gitignore.lang.kind.GitLanguage
import mobi.hsz.idea.gitignore.settings.IgnoreSettings
import mobi.hsz.idea.gitignore.util.Constants
import mobi.hsz.idea.gitignore.util.Properties
import mobi.hsz.idea.gitignore.util.exec.ExternalExec

/**
 * Editor notification provider that suggests to add unversioned files to the .gitignore file.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.4
 */
class AddUnversionedFilesNotificationProvider
/**
 * Builds a new instance of [AddUnversionedFilesNotificationProvider].
 *
 * @param project       current project
 * @param notifications notifications component
 */
(
        /** Current project.  */
        private val project: Project,
        /** Notifications component.  */
        private val notifications: EditorNotifications) : EditorNotifications.Provider<EditorNotificationPanel>() {

    /** Plugin settings holder.  */
    private val settings: IgnoreSettings = IgnoreSettings.getInstance()

    /** List of unignored files.  */
    private val unignoredFiles = ContainerUtil.newArrayList<String>()

    /** Map to obtain if file was handled.  */
    private val handledMap = WeakKeyWeakValueHashMap<VirtualFile, Boolean>()

    /**
     * Gets notification key.
     *
     * @return notification key
     */
    override fun getKey(): Key<EditorNotificationPanel> = KEY

    /**
     * Creates notification panel for given file and checks if is allowed to show the notification.
     * Only [GitLanguage] is currently supported.
     *
     * @param file       current file
     * @param fileEditor current file editor
     * @return created notification panel
     */
    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel? {
        // Break if feature is disabled in the Settings
        when {
            !settings.isAddUnversionedFiles -> return null
        // Break if user canceled previously this notification
            Properties.isAddUnversionedFiles(project) -> return null
            handledMap[file] != null -> return null
            else -> {
                val language = IgnoreBundle.obtainLanguage(file)
                if (language == null || !language.isVCS || language !is GitLanguage) {
                    return null
                }

                unignoredFiles.clear()
                unignoredFiles.addAll(ExternalExec.getUnignoredFiles(GitLanguage.INSTANCE, project, file))

                return when {
                    unignoredFiles.isEmpty() -> null
                    else -> createPanel(project)
                }
            }
        }
    }

    /**
     * Creates notification panel.
     *
     * @param project current project
     * @return notification panel
     */
    private fun createPanel(project: Project): EditorNotificationPanel {
        val panel = EditorNotificationPanel()
        val fileType = GitFileType.INSTANCE
        panel.setText(IgnoreBundle.message("daemon.addUnversionedFiles"))
        panel.createActionLabel(IgnoreBundle.message("daemon.addUnversionedFiles.create")) {
            val virtualFile = project.baseDir.findChild(GitLanguage.INSTANCE.filename)
            val file = if (virtualFile != null) PsiManager.getInstance(project).findFile(virtualFile) else null
            if (file != null) {
                val content = StringUtil.join(unignoredFiles, Constants.NEWLINE)

                AppendFileCommandAction(project, file, content, true, false).execute()
                handledMap.put(virtualFile, true)
                notifications.updateAllNotifications()
            }
        }
        panel.createActionLabel(IgnoreBundle.message("daemon.cancel")) {
            Properties.setAddUnversionedFiles(project)
            notifications.updateAllNotifications()
        }

        try { // ignore if older SDK does not support panel icon
            val icon = fileType.icon
            if (icon != null) {
                panel.icon(icon)
            }
        } catch (ignored: NoSuchMethodError) {
        }

        return panel
    }

    companion object {
        /** Notification key.  */
        private val KEY = Key.create<EditorNotificationPanel>("AddUnversionedFilesNotificationProvider")
    }
}
