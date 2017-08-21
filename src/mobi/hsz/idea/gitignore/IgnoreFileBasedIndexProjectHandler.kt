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

import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerAdapter
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.IndexableFileSet
import mobi.hsz.idea.gitignore.IgnoreManager.RefreshStatusesListener.REFRESH_STATUSES
import mobi.hsz.idea.gitignore.file.type.IgnoreFileType
import mobi.hsz.idea.gitignore.indexing.ExternalIndexableSetContributor

/**
 * Project component that registers [IndexableFileSet] that counts into indexing files located outside of the
 * project.
 *
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 2.0
 */
class IgnoreFileBasedIndexProjectHandler

/**
 * Constructor.
 *
 * @param project        current project
 * @param projectManager project manager instance
 * @param index          index instance
 */
(project: Project,
 /** [ProjectManager] instance. */
 private val projectManager: ProjectManager,
 /** [FileBasedIndex] instance. */
 private val index: FileBasedIndex) : AbstractProjectComponent(project), IndexableFileSet {
    /** Project listener to remove [IndexableFileSet] from the indexable sets. */
    private val projectListener = object : ProjectManagerAdapter() {
        override fun projectClosing(project: Project?) {
            index.removeIndexableSet(this@IgnoreFileBasedIndexProjectHandler)
        }
    }

    init {
        StartupManager.getInstance(myProject).registerPreStartupActivity {
            index.registerIndexableSet(this@IgnoreFileBasedIndexProjectHandler, project)
            myProject.messageBus.syncPublisher<IgnoreManager.RefreshStatusesListener>(REFRESH_STATUSES).refresh()
        }
    }

    /** Initialize component and add [.projectListener]. */
    override fun initComponent() {
        projectManager.addProjectManagerListener(myProject, projectListener)
    }

    /** Dispose component and remove [.projectListener]. */
    override fun disposeComponent() {
        projectManager.removeProjectManagerListener(myProject, projectListener)
    }

    /**
     * Checks if given file is in [ExternalIndexableSetContributor] set.
     *
     * @param file to check
     * @return is in set
     */
    override fun isInSet(file: VirtualFile): Boolean = file.fileType is IgnoreFileType && ExternalIndexableSetContributor.getAdditionalFiles(myProject).contains(file)

    /**
     * Iterates over given file's children.
     *
     * @param file     to iterate
     * @param iterator iterator
     */
    override fun iterateIndexableFilesIn(file: VirtualFile, iterator: ContentIterator) {
        VfsUtilCore.visitChildrenRecursively(file, object : VirtualFileVisitor<Any>() {
            override fun visitFile(file: VirtualFile): Boolean {
                when {
                    !isInSet(file) -> return false
                    !file.isDirectory -> iterator.processFile(file)
                }
                return true
            }
        })
    }
}
