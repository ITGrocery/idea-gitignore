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

package mobi.hsz.idea.gitignore.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.psi.IgnoreSyntax

/**
 * Class provides completion feature for [mobi.hsz.idea.gitignore.psi.IgnoreTypes.SYNTAX] element.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.0
 */
class SyntaxCompletionContributor : CompletionContributor() {
    init {
        extend(
                CompletionType.BASIC,
                StandardPatterns.instanceOf(PsiElement::class.java),
                object : CompletionProvider<CompletionParameters>() {
                    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext,
                                                result: CompletionResultSet) {
                        val current = parameters.position
                        if (current.parent is IgnoreSyntax && current.prevSibling != null) {
                            result.addAllElements(SYNTAX_ELEMENTS)
                        }
                    }
                }
        )
    }

    /** Allow autoPopup to appear after custom symbol. */
    override fun invokeAutoPopup(position: PsiElement, typeChar: Char): Boolean = true

    companion object {
        /** Allowed values for the completion. */
        private val SYNTAX_ELEMENTS = ContainerUtil.newArrayList<LookupElementBuilder>()

        init {
            IgnoreBundle.Syntax.values().mapTo(SYNTAX_ELEMENTS) { LookupElementBuilder.create(it.toString().toLowerCase()) }
        }
    }
}
