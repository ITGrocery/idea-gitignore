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
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.command.CreateFileCommandAction
import mobi.hsz.idea.gitignore.file.type.kind.GitFileType
import mobi.hsz.idea.gitignore.lang.kind.GitLanguage
import mobi.hsz.idea.gitignore.settings.IgnoreSettings
import mobi.hsz.idea.gitignore.ui.GeneratorDialog
import mobi.hsz.idea.gitignore.util.Properties

/**
 * Editor notification provider that checks if there is [GitLanguage.getFilename]
 * in root directory and suggest to create one.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 0.3.3
 */
class MissingGitignoreNotificationProvider
/**
 * Builds a new instance of [MissingGitignoreNotificationProvider].
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

    /**
     * Gets notification key.
     *
     * @return notification key
     */
    override fun getKey(): Key<EditorNotificationPanel> = KEY

    /**
     * Creates notification panel for given file and checks if is allowed to show the notification.
     *
     * @param file       current file
     * @param fileEditor current file editor
     * @return created notification panel
     */
    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel? {
        // Break if feature is disabled in the Settings
        if (!settings.isMissingGitignore) {
            return null
        }
        // Break if user canceled previously this notification
        if (Properties.isIgnoreMissingGitignore(project)) {
            return null
        }
        // Break if there is no Git directory in the project
        val vcsDirectory = GitLanguage.INSTANCE.vcsDirectory ?: return null
        val baseDir = project.baseDir ?: return null
        val gitDirectory = baseDir.findChild(vcsDirectory)
        if (gitDirectory == null || !gitDirectory.isDirectory) {
            return null
        }
        // Break if there is Gitignore file already
        val gitignoreFile = baseDir.findChild(GitLanguage.INSTANCE.filename)
        return when {
            gitignoreFile != null -> null
            else -> createPanel(project)
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
        panel.setText(IgnoreBundle.message("daemon.missingGitignore"))
        panel.createActionLabel(IgnoreBundle.message("daemon.missingGitignore.create")) {
            val directory = PsiManager.getInstance(project).findDirectory(project.baseDir)
            if (directory != null) {
                val file = CreateFileCommandAction(project, directory, fileType).execute().resultObject
                FileEditorManager.getInstance(project).openFile(file.virtualFile, true)
                GeneratorDialog(project, file).show()
            }
        }
        panel.createActionLabel(IgnoreBundle.message("daemon.cancel")) {
            Properties.setIgnoreMissingGitignore(project)
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
        private val KEY = Key.create<EditorNotificationPanel>("MissingGitignoreNotificationProvider")
    }
}
