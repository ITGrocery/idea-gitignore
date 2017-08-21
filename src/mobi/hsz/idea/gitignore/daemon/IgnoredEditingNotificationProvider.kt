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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.IgnoreManager
import mobi.hsz.idea.gitignore.settings.IgnoreSettings
import mobi.hsz.idea.gitignore.util.Icons
import mobi.hsz.idea.gitignore.util.Properties

/**
 * Editor notification provider that informs about the attempt of the ignored file modification.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.8
 */
class IgnoredEditingNotificationProvider
/**
 * Builds a new instance of [IgnoredEditingNotificationProvider].
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

    /** [IgnoreManager] instance.  */
    private val manager: IgnoreManager = IgnoreManager.getInstance(project)

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
        if (!settings.isNotifyIgnoredEditing || !manager.isFileIgnored(file) ||
                Properties.isDismissedIgnoredEditingNotification(project, file)) {
            return null
        }

        val panel = EditorNotificationPanel()

        panel.setText(IgnoreBundle.message("daemon.ignoredEditing"))
        panel.createActionLabel(IgnoreBundle.message("daemon.ok")) {
            Properties.setDismissedIgnoredEditingNotification(project, file)
            notifications.updateAllNotifications()
        }

        try { // ignore if older SDK does not support panel icon
            panel.icon(Icons.IGNORE)
        } catch (ignored: NoSuchMethodError) {
        }

        return panel
    }

    companion object {
        /** Notification key.  */
        private val KEY = Key.create<EditorNotificationPanel>("IgnoredEditingNotificationProvider")
    }
}
