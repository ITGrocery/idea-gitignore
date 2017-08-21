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

import com.intellij.psi.PsiElement
import mobi.hsz.idea.gitignore.IgnoreBundle

import java.util.regex.Pattern

/**
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.0
 */
interface IgnoreEntryBase : PsiElement {
    /**
     * Checks if current element is negated.
     *
     * @return is negated
     */
    val isNegated: Boolean

    /**
     * Returns current element's syntax.
     *
     * @return current syntax
     *
     * @see {@link IgnoreBundle.Syntax}
     */
    val syntax: IgnoreBundle.Syntax

    /**
     * Returns current value.
     *
     * @return value
     */
    val value: String

    /**
     * Returns current pattern.
     *
     * @return pattern
     */
    val pattern: Pattern?
}
