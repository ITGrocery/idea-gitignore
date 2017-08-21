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

package mobi.hsz.idea.gitignore.vcs

import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.FileStatusFactory
import com.intellij.openapi.vcs.impl.FileStatusProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.util.ThreeState
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.IgnoreManager

/**
 * Ignore instance of [FileStatusProvider] that provides [.IGNORED] status
 * for the files matched with ignore rules.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.0
 */
class IgnoreFileStatusProvider(project: Project) : FileStatusProvider, DumbAware {
    /** Instance of [IgnoreManager]. */
    private val ignoreManager: IgnoreManager = IgnoreManager.getInstance(project)

    /**
     * Returns the [.IGNORED] status if file is ignored or `null`.
     *
     * @param virtualFile file to check
     * @return [.IGNORED] status or `null`
     */
    override fun getFileStatus(virtualFile: VirtualFile): FileStatus? =
            when {
                ignoreManager.isFileIgnored(virtualFile) && !ignoreManager.isFileTracked(virtualFile) -> IGNORED
                else -> null
            }

    /** Does nothing. */
    override fun refreshFileStatusFromDocument(virtualFile: VirtualFile, doc: Document) {}

    /**
     * Does nothing.
     *
     * @param virtualFile file
     * @return nothing
     */
    override fun getNotChangedDirectoryParentingStatus(virtualFile: VirtualFile): ThreeState {
        throw UnsupportedOperationException("Shouldn't be called")
    }

    companion object {
        /** Ignored status. */
        val IGNORED: FileStatus = FileStatusFactory.getInstance().createFileStatus(
                "IGNORE.PROJECT_VIEW.IGNORED", IgnoreBundle.message("projectView.ignored"), JBColor.GRAY)
    }
}
