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

package mobi.hsz.idea.gitignore.file

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.util.IncorrectOperationException
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.file.type.IgnoreFileType
import mobi.hsz.idea.gitignore.util.Constants

/**
 * Templates factory that generates Gitignore file and its content.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 0.1
 */
class IgnoreTemplatesFactory
/** Builds a new instance of [IgnoreTemplatesFactory].  */
(
        /** Current file type.  */
        private val fileType: IgnoreFileType) : FileTemplateGroupDescriptorFactory {

    /** Group descriptor.  */
    private val templateGroup: FileTemplateGroupDescriptor = FileTemplateGroupDescriptor(
            fileType.ignoreLanguage.id,
            fileType.icon
    )

    init {
        templateGroup.addTemplate(fileType.ignoreLanguage.filename)
    }

    /**
     * Returns group descriptor.
     *
     * @return descriptor
     */
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor = templateGroup

    /**
     * Creates new Gitignore file or uses an existing one.
     *
     * @param directory working directory
     * @return file
     *
     * @throws IncorrectOperationException
     */
    @Throws(IncorrectOperationException::class)
    fun createFromTemplate(directory: PsiDirectory): PsiFile? {
        val filename = fileType.ignoreLanguage.filename
        val currentFile = directory.findFile(filename)
        if (currentFile != null) {
            return currentFile
        }

        val factory = PsiFileFactory.getInstance(directory.project)
        val language = fileType.ignoreLanguage
        var content = StringUtil.join(TEMPLATE_NOTE, Constants.NEWLINE)
        if (language.isSyntaxSupported && IgnoreBundle.Syntax.GLOB != language.defaultSyntax) {
            content = StringUtil.join(
                    content,
                    IgnoreBundle.Syntax.GLOB.presentation,
                    Constants.NEWLINE,
                    Constants.NEWLINE
            )
        }
        val file = factory.createFileFromText(filename, fileType, content)
        return directory.add(file) as PsiFile
    }

    companion object {
        /** File's content header.  */
        private val TEMPLATE_NOTE = IgnoreBundle.message("file.templateNote")
    }
}
