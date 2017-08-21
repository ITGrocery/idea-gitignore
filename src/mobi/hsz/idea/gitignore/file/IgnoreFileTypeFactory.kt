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

import com.intellij.openapi.fileTypes.ExactFileNameMatcher
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.file.type.IgnoreFileType

/**
 * Class that assigns file types with languages.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 0.1
 */
class IgnoreFileTypeFactory : FileTypeFactory() {
    /**
     * Assigns file types with languages.
     *
     * @param consumer file types consumer
     */
    override fun createFileTypes(consumer: FileTypeConsumer) {
        consume(consumer, IgnoreFileType.INSTANCE)
        IgnoreBundle.LANGUAGES.forEach { language -> consume(consumer, language.fileType) }
    }

    /**
     * Shorthand for consuming ignore file types.
     *
     * @param consumer file types consumer
     * @param fileType file type to consume
     */
    private fun consume(consumer: FileTypeConsumer, fileType: IgnoreFileType) {
        consumer.consume(fileType, ExactFileNameMatcher(fileType.ignoreLanguage.filename))
        consumer.consume(fileType, fileType.ignoreLanguage.extension)
    }
}
