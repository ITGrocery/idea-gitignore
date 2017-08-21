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

package mobi.hsz.idea.gitignore.outer

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.ContainerUtil
import mobi.hsz.idea.gitignore.file.type.IgnoreFileType
import mobi.hsz.idea.gitignore.lang.kind.GitExcludeLanguage
import mobi.hsz.idea.gitignore.lang.kind.GitLanguage
import mobi.hsz.idea.gitignore.settings.IgnoreSettings
import mobi.hsz.idea.gitignore.settings.IgnoreSettings.KEY
import org.jetbrains.annotations.NonNls

/**
 * Component loader for outer ignore files editor.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.1
 */
class OuterIgnoreLoaderComponent
/** Constructor.  */
(project: Project) : AbstractProjectComponent(project) {

    /**
     * Returns component name.
     *
     * @return component name
     */
    @NonNls
    override fun getComponentName(): String = "IgnoreOuterComponent"

    /** Initializes component.  */
    override fun initComponent() {
        myProject.messageBus.connect().subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER,
                IgnoreEditorManagerListener(myProject)
        )
    }

    /** Listener for ignore editor manager.  */
    private inner class IgnoreEditorManagerListener
    /** Constructor.  */
    (
            /** Current project.  */
            private val project: Project) : FileEditorManagerListener {

        /**
         * Handles file opening event and attaches outer ignore component.
         *
         * @param source editor manager
         * @param file   current file
         */
        override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
            val fileType = file.fileType
            if (fileType !is IgnoreFileType || !IgnoreSettings.getInstance().isOuterIgnoreRules) {
                return
            }

            val language = fileType.ignoreLanguage
            if (!language.isEnabled) {
                return
            }

            DumbService.getInstance(project).runWhenSmart(Runnable {
                val outerFiles = ContainerUtil.newArrayList(language.getOuterFiles(myProject))
                if (language is GitLanguage) {
                    ContainerUtil.addAllNotNull(outerFiles, GitExcludeLanguage.INSTANCE.getOuterFiles(myProject))
                }
                if (outerFiles.isEmpty() || outerFiles.contains(file)) {
                    return@Runnable
                }

                for (fileEditor in source.getEditors(file)) {
                    if (fileEditor is TextEditor) {
                        val wrapper = OuterIgnoreWrapper(project, language, outerFiles)
                        val c = wrapper.component
                        source.addBottomComponent(fileEditor, c)

                        IgnoreSettings.getInstance().addListener { key, value ->
                            if (KEY.OUTER_IGNORE_RULES == key) {
                                c.isVisible = value as Boolean
                            }
                        }

                        Disposer.register(fileEditor, wrapper)
                        Disposer.register(fileEditor, Disposable { source.removeBottomComponent(fileEditor, c) })
                    }
                }
            })
        }

        override fun fileClosed(source: FileEditorManager, file: VirtualFile) {}

        override fun selectionChanged(event: FileEditorManagerEvent) {}
    }

    /** Outer file fetcher event interface.  */
    interface OuterFileFetcher {
        fun fetch(project: Project): Collection<VirtualFile>
    }

    companion object {
        /**
         * Returns [OuterIgnoreLoaderComponent] service instance.
         *
         * @param project current project
         * @return [instance][OuterIgnoreLoaderComponent]
         */
        fun getInstance(project: Project): OuterIgnoreLoaderComponent {
            return project.getComponent(OuterIgnoreLoaderComponent::class.java)
        }
    }
}
