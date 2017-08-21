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

import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.util.containers.ContainerUtil
import mobi.hsz.idea.gitignore.file.type.IgnoreFileType
import mobi.hsz.idea.gitignore.lang.IgnoreLanguage

/**
 * Base plugin file.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 0.8
 */
class IgnoreFile
/** Builds a new instance of [IgnoreFile].  */
(viewProvider: FileViewProvider,
 /** Current file type.  */
 private val fileType: IgnoreFileType) : PsiFileImpl(viewProvider) {
    /** Current language.  */
    private val language: Language

    /** Current parser definition.  */
    /**
     * Returns current parser definition.
     *
     * @return current [ParserDefinition]
     */
    private val parserDefinition: ParserDefinition

    init {
        this.language = findLanguage(fileType.language, viewProvider)

        val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(this.language) ?: throw RuntimeException(
                "PsiFileBase: language.getParserDefinition() returned null for: " + this.language
        )
        this.parserDefinition = parserDefinition

        val nodeType = parserDefinition.fileNodeType
        init(nodeType, nodeType)
    }

    /**
     * Searches for the matching language in [FileViewProvider].
     *
     * @param baseLanguage language to look for
     * @param viewProvider current [FileViewProvider]
     * @return matched [Language]
     */
    private fun findLanguage(baseLanguage: Language, viewProvider: FileViewProvider): Language {
        val languages = viewProvider.languages

        languages
                .filter { it.isKindOf(baseLanguage) }
                .forEach { return it }

        languages
                .filterIsInstance<IgnoreLanguage>()
                .forEach { return it }

        throw AssertionError("Language " + baseLanguage + " doesn't participate in view provider " +
                viewProvider + ": " + ContainerUtil.newArrayList(languages))
    }

    /**
     * Passes the element to the specified visitor.
     *
     * @param visitor the visitor to pass the element to.
     */
    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }

    /**
     * Returns current language.
     *
     * @return current [Language]
     */
    override fun getLanguage(): Language = language

    /**
     * Returns the file type for the file.
     *
     * @return the file type instance.
     */
    override fun getFileType(): FileType = fileType

    /**
     * Checks if current file is the language outer file.
     *
     * @return is outer file
     */
    val isOuter: Boolean
        get() {
            val outerFiles = fileType.ignoreLanguage.getOuterFiles(project)
            return outerFiles.contains(originalFile.virtualFile)
        }

    /**
     * Returns @{link IgnoreFileType} string interpretation.
     *
     * @return string interpretation
     */
    override fun toString(): String = fileType.name
}
