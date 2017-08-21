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

package mobi.hsz.idea.gitignore.util.exec.parser

import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.containers.ContainerUtil
import java.util.*

/**
 * Abstract output parser for the ExternalExec process outputs.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.5
 */
abstract class ExecutionOutputParser<T> {
    /** Outputs list.  */
    /**
     * Returns collected output.
     *
     * @return parsed output
     */
    val output: ArrayList<T>? = ContainerUtil.newArrayList()

    /** Exit code value.  */
    private var exitCode: Int = 0

    /** Error occurred during the output parsing.  */
    private var errorsReported: Boolean = false

    /**
     * Handles single output line.
     *
     * @param text       execution response
     * @param outputType output type
     */
    fun onTextAvailable(text: String, outputType: Key<*>) {
        if (outputType === ProcessOutputTypes.SYSTEM) {
            return
        }

        if (outputType === ProcessOutputTypes.STDERR) {
            errorsReported = true
            return
        }

        ContainerUtil.addIfNotNull(output!!, parseOutput(StringUtil.trimEnd(text, "\n").trim { it <= ' ' }))
    }

    /**
     * Main method that parses output for the specified result data.
     *
     * @param text input data
     * @return single parsed result
     */
    protected abstract fun parseOutput(text: String): T?

    /**
     * Method called at the end of the parsing process.
     *
     * @param exitCode result of the executable call
     */
    fun notifyFinished(exitCode: Int) {
        this.exitCode = exitCode
    }

    /**
     * Checks if any error occurred during the parsing.
     *
     * @return error was reported
     */
    val isErrorsReported: Boolean
        get() = errorsReported || exitCode != 0
}
