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

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.SeveritiesProvider
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import com.intellij.util.containers.ContainerUtil
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.highlighter.IgnoreHighlighterColors
import java.awt.Color

/**
 * Severities provider that checks if entry points to any file or directory.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 0.5.4
 */
class UnusedEntrySeveritiesProvider : SeveritiesProvider() {
    /**
     * Defines the style of matched entry.
     *
     * @return style definition
     */
    override fun getSeveritiesHighlightInfoTypes(): List<HighlightInfoType> {
        val result = ContainerUtil.newArrayList<HighlightInfoType>()
        result.add(HighlightInfoType.HighlightInfoTypeImpl(
                UNUSED_ENTRY,
                TextAttributesKey.createTextAttributesKey(
                        IgnoreBundle.message("codeInspection.unusedEntry"),
                        IgnoreHighlighterColors.UNUSED_ENTRY
                )
        ))
        return result
    }

    /**
     * Defines color of the matched entry.
     *
     * @param textAttributes current attribute
     * @return entry color
     */
    override fun getTrafficRendererColor(textAttributes: TextAttributes): Color = JBColor.GRAY

    /**
     * Checks if severity goto is enabled.
     *
     * @param minSeverity severity to compare
     * @return severity equals to the [.UNUSED_ENTRY]
     */
    override fun isGotoBySeverityEnabled(minSeverity: HighlightSeverity?): Boolean = UNUSED_ENTRY !== minSeverity

    companion object {
        /** Unused entry [HighlightSeverity] instance.  */
        private val UNUSED_ENTRY = HighlightSeverity("UNUSED ENTRY", 10)
    }
}
