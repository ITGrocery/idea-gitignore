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

package mobi.hsz.idea.gitignore.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import mobi.hsz.idea.gitignore.file.type.IgnoreFileType
import mobi.hsz.idea.gitignore.lang.IgnoreLanguage

/**
 * Entry manipulator.
 *
 * @author Alexander Zolotov <alexander.zolotov></alexander.zolotov>@jetbrains.com>
 * @since 0.5
 */
class IgnoreEntryManipulator : AbstractElementManipulator<IgnoreEntry>() {
    /**
     * Changes the element's text to a new value
     *
     * @param entry      element to be changed
     * @param range      range within the element
     * @param newContent new element text
     * @return changed element
     *
     * @throws IncorrectOperationException if something goes wrong
     */
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(entry: IgnoreEntry, range: TextRange, newContent: String): IgnoreEntry {
        if (entry.language !is IgnoreLanguage) {
            return entry
        }
        val language = entry.language as IgnoreLanguage
        val fileType = (language.associatedFileType as IgnoreFileType?)!!
        val file = PsiFileFactory.getInstance(entry.project)
                .createFileFromText(language.filename, fileType, range.replace(entry.text, newContent))
        val newEntry = PsiTreeUtil.findChildOfType(file, IgnoreEntry::class.java)!!
        return entry.replace(newEntry) as IgnoreEntry
    }

    /**
     * Returns range of the entry. Skips negation element.
     *
     * @param element element to be changed
     * @return range
     */
    override fun getRangeInElement(element: IgnoreEntry): TextRange {
        val negation = element.negation
        return when {
            negation != null -> TextRange.create(
                    negation.startOffsetInParent + negation.textLength,
                    element.textLength
            )
            else -> super.getRangeInElement(element)
        }
    }
}
