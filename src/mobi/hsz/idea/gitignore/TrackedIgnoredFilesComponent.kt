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

package mobi.hsz.idea.gitignore

import com.intellij.dvcs.repo.Repository
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection
import mobi.hsz.idea.gitignore.settings.IgnoreSettings
import mobi.hsz.idea.gitignore.ui.untrackFiles.UntrackFilesDialog
import mobi.hsz.idea.gitignore.util.Notify
import mobi.hsz.idea.gitignore.util.Utils
import org.jetbrains.annotations.NonNls
import java.util.concurrent.ConcurrentMap

/**
 * ProjectComponent instance to handle [IgnoreManager.TrackedIgnoredListener] event
 * and display [Notification] about tracked and ignored files which invokes [UntrackFilesDialog].
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.7
 */
class TrackedIgnoredFilesComponent

/**
 * Constructor.
 *
 * @param project current project
 */
private constructor(project: Project) : AbstractProjectComponent(project), IgnoreManager.TrackedIgnoredListener {
    /** [MessageBusConnection] instance. */
    private var messageBus: MessageBusConnection? = null

    /** [IgnoreSettings] instance. */
    private var settings: IgnoreSettings? = null

    /** Notification about tracked files was shown for current project. */
    private var notificationShown: Boolean = false

    /** Component initialization method. */
    override fun initComponent() {
        settings = IgnoreSettings.getInstance()
        messageBus = myProject.messageBus.connect()
        messageBus!!.subscribe<IgnoreManager.TrackedIgnoredListener>(IgnoreManager.TrackedIgnoredListener.TRACKED_IGNORED, this)
    }

    /** Component dispose method. */
    override fun disposeComponent() {
        if (messageBus != null) {
            messageBus!!.disconnect()
        }
    }

    /**
     * Returns component's name.
     *
     * @return component's name
     */
    override fun getComponentName(): String = "TrackedIgnoredFilesComponent"

    /**
     * [IgnoreManager.TrackedIgnoredListener] method implementation to handle incoming
     * files.
     *
     * @param files tracked and ignored files list
     */
    override fun handleFiles(files: ConcurrentMap<VirtualFile, Repository>) {
        when {
            !settings!!.isInformTrackedIgnored || notificationShown || myProject.baseDir == null -> return
            else -> {
                notificationShown = true
                Notify.show(
                        myProject,
                        IgnoreBundle.message("notification.untrack.title", Utils.getVersion()),
                        IgnoreBundle.message("notification.untrack.content"),
                        NotificationType.WARNING
                ) { notification, event ->
                    when (DISABLE_ACTION) {
                        event.description -> settings!!.isInformTrackedIgnored = false
                        else -> UntrackFilesDialog(myProject, files).show()
                    }
                    notification.expire()
                }
            }
        }
    }

    companion object {
        /** Disable action event. */
        @NonNls
        private val DISABLE_ACTION = "#disable"
    }
}
