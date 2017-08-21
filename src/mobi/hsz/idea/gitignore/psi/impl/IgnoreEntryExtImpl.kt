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

package mobi.hsz.idea.gitignore.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.lang.IgnoreLanguage
import mobi.hsz.idea.gitignore.psi.*
import mobi.hsz.idea.gitignore.util.Glob

import java.util.regex.Pattern

/**
 * Custom [IgnoreElementImpl] implementation.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 0.5
 */
abstract class IgnoreEntryExtImpl
/** Constructor.  */
(node: ASTNode) : IgnoreElementImpl(node), IgnoreEntry {

    /**
     * Checks if the first child is negated - i.e. `!file.txt` entry.
     *
     * @return first child is negated
     */
    override val isNegated: Boolean
        get() = firstChild is IgnoreNegation

    /**
     * Checks if current entry is a directory - i.e. `dir/`.
     *
     * @return is directory
     */
    val isDirectory: Boolean
        get() = this is IgnoreEntryFile

    /**
     * Checks if current entry is a file - i.e. `file.txt`.
     *
     * @return is file
     *
     * @see {@link .isDirectory
     */
    val isFile: Boolean
        get() = !isDirectory

    /**
     * Returns element syntax.
     *
     * @return syntax
     */
    override val syntax: IgnoreBundle.Syntax
        get() {
            var previous: PsiElement? = prevSibling
            while (previous != null) {
                if (previous.node.elementType == IgnoreTypes.SYNTAX) {
                    val syntax = IgnoreBundle.Syntax.find((previous as IgnoreSyntaxImpl).value.text)
                    if (syntax != null) {
                        return syntax
                    }
                }
                previous = previous.prevSibling
            }
            return (containingFile.language as IgnoreLanguage).defaultSyntax
        }

    /**
     * Returns entry value without leading `!` if entry is negated.
     *
     * @return entry value without `!` negation sign
     */
    override val value: String
        get() {
            var value = text
            if (isNegated) {
                value = StringUtil.trimStart(value, "!")
            }
            return value
        }

    /**
     * Returns entries pattern.
     *
     * @return pattern
     */
    override val pattern: Pattern?
        get() = Glob.createPattern(this)
}
