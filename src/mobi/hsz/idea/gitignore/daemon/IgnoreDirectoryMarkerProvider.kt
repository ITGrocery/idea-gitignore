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

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.util.PlatformIcons
import mobi.hsz.idea.gitignore.psi.IgnoreEntryDirectory
import mobi.hsz.idea.gitignore.psi.IgnoreEntryFile
import mobi.hsz.idea.gitignore.util.Glob
import mobi.hsz.idea.gitignore.util.Utils

/**
 * [LineMarkerProvider] that marks entry lines with directory icon if they point to the directory in virtual
 * system.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 0.5
 */
class IgnoreDirectoryMarkerProvider : LineMarkerProvider {
    /**
     * Returns [LineMarkerInfo] with set [PlatformIcons.FOLDER_ICON] if entry points to the directory.
     *
     * @param element current element
     * @return `null` if entry is not a directory
     */
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        var isDirectory = element is IgnoreEntryDirectory
        if (!isDirectory && element is IgnoreEntryFile) {
            val parent = element.getContainingFile().virtualFile.parent
            val project = element.getProject()
            val projectDir = project.baseDir
            if (parent == null || projectDir == null || !Utils.isUnder(parent, projectDir)) {
                return null
            }
            val files = Glob.findOne(parent, element)
            files
                    .filterNot { it.isDirectory }
                    .forEach { return null }
            isDirectory = files.size > 0
        }

        return when {
            isDirectory -> LineMarkerInfo(element, element.textRange, PlatformIcons.FOLDER_ICON, Pass.UPDATE_ALL, null, null, GutterIconRenderer.Alignment.CENTER)
            else -> null
        }
    }

    /**
     * Mocked method.
     *
     * @param elements unused parameter
     * @param result   unused parameter
     */
    override fun collectSlowLineMarkers(elements: List<PsiElement>, result: Collection<LineMarkerInfo<*>>) {}
}
