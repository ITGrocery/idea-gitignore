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

package mobi.hsz.idea.gitignore.util.exec

import com.intellij.dvcs.repo.Repository
import com.intellij.execution.process.BaseOSProcessHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.ContainerUtil
import git4idea.config.GitVcsApplicationSettings
import mobi.hsz.idea.gitignore.lang.IgnoreLanguage
import mobi.hsz.idea.gitignore.lang.kind.GitLanguage
import mobi.hsz.idea.gitignore.util.Utils
import mobi.hsz.idea.gitignore.util.exec.parser.ExecutionOutputParser
import mobi.hsz.idea.gitignore.util.exec.parser.GitExcludesOutputParser
import mobi.hsz.idea.gitignore.util.exec.parser.GitUnignoredFilesOutputParser
import mobi.hsz.idea.gitignore.util.exec.parser.IgnoredFilesParser
import org.jetbrains.annotations.NonNls
import org.jetbrains.jps.service.SharedThreadPool
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Future

/**
 * Class that holds util methods for calling external executables (i.e. git/hg)
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.4
 */
object ExternalExec {
    /** Default external exec timeout.  */
    private val DEFAULT_TIMEOUT = 5000

    /** Checks if Git plugin is enabled.  */
    private val GIT_ENABLED = Utils.isGitPluginEnabled()

    /** Git command to get user's excludesfile path.  */
    @NonNls
    private val GIT_CONFIG_EXCLUDES_FILE = "config --global core.excludesfile"

    /** Git command to list unversioned files.  */
    @NonNls
    private val GIT_UNIGNORED_FILES = "clean -dn"

    /** Git command to list ignored but tracked files.  */
    @NonNls
    private val GIT_IGNORED_FILES = "status --ignored --porcelain"

    /** Git command to remove file from tracking.  */
    @NonNls
    private val GIT_REMOVE_FILE_FROM_TRACKING = "rm --cached --force"

    /**
     * Returns [VirtualFile] instance of the Git excludes file if available.
     *
     * @return Git excludes file
     */
    val gitExcludesFile: VirtualFile?
        get() = runForSingle(GitLanguage.INSTANCE, GIT_CONFIG_EXCLUDES_FILE, null, GitExcludesOutputParser())

    /**
     * Returns list of unignored files for the given directory.
     *
     * @param language to check
     * @param project  current project
     * @param file     current file
     * @return unignored files list
     */
    fun getUnignoredFiles(language: IgnoreLanguage, project: Project, file: VirtualFile): List<String> = when {
        !Utils.isInProject(file, project) -> ContainerUtil.newArrayList()
        else -> Utils.notNullize(run(
                language,
                GIT_UNIGNORED_FILES,
                file.parent,
                GitUnignoredFilesOutputParser()
        ))
    }

    /**
     * Returns list of ignored files for the given repository.
     *
     * @param repository repository to check
     * @return unignored files list
     */
    fun getIgnoredFiles(repository: Repository): List<String> = Utils.notNullize(run(
            GitLanguage.INSTANCE,
            GIT_IGNORED_FILES,
            repository.root,
            IgnoredFilesParser()
    ))

    /**
     * Removes given files from the git tracking.
     *
     * @param file       to untrack
     * @param repository file's repository
     */
    fun removeFileFromTracking(file: VirtualFile, repository: Repository) {
        val root = repository.root
        val command = GIT_REMOVE_FILE_FROM_TRACKING + " " + Utils.getRelativePath(root, file)
        run(GitLanguage.INSTANCE, command, root)
    }

    /**
     * Returns path to the [IgnoreLanguage] binary or null if not available.
     * Currently only  [GitLanguage] is supported.
     *
     * @param language current language
     * @return path to binary
     */
    private fun bin(language: IgnoreLanguage): String? = when {
        GitLanguage.INSTANCE == language && GIT_ENABLED -> {
            val bin = GitVcsApplicationSettings.getInstance().pathToGit
            StringUtil.nullize(bin)
        }
        else -> null
    }

    /**
     * Runs [IgnoreLanguage] executable with the given command and current working directory.
     *
     * @param language  current language
     * @param command   to call
     * @param directory current working directory
     * @param parser    [ExecutionOutputParser] implementation
     * @param <T>       return type
     * @return result of the call
    </T> */
    private fun <T> runForSingle(language: IgnoreLanguage, command: String,
                                 directory: VirtualFile?, parser: ExecutionOutputParser<T>): T? =
            Utils.getFirstItem(run(language, command, directory, parser))

    /**
     * Runs [IgnoreLanguage] executable with the given command and current working directory.
     *
     * @param language  current language
     * @param command   to call
     * @param directory current working directory
     */
    private fun run(language: IgnoreLanguage,
                    command: String,
                    directory: VirtualFile?) {
        run<Any>(language, command, directory, null)
    }

    /**
     * Runs [IgnoreLanguage] executable with the given command and current working directory.
     *
     * @param language  current language
     * @param command   to call
     * @param directory current working directory
     * @param parser    [ExecutionOutputParser] implementation
     * @param <T>       return type
     * @return result of the call
    </T> */
    private fun <T> run(language: IgnoreLanguage,
                        command: String,
                        directory: VirtualFile?,
                        parser: ExecutionOutputParser<T>?): ArrayList<T>? {
        val bin = bin(language) ?: return null

        try {
            val cmd = bin + " " + command
            val workingDirectory = if (directory != null) File(directory.path) else null
            val process = Runtime.getRuntime().exec(cmd, null, workingDirectory)

            val handler = object : BaseOSProcessHandler(process, StringUtil.join(cmd, " "), null) {
                override fun executeOnPooledThread(task: Runnable): Future<*> =
                        SharedThreadPool.getInstance().executeOnPooledThread(task)

                override fun notifyTextAvailable(text: String, outputType: Key<*>) {
                    parser?.onTextAvailable(text, outputType)
                }
            }

            handler.startNotify()
            if (!handler.waitFor(DEFAULT_TIMEOUT.toLong())) {
                return null
            }
            if (parser != null) {
                parser.notifyFinished(process.exitValue())
                return when {
                    parser.isErrorsReported -> null
                    else -> parser.output
                }
            }
        } catch (ignored: IOException) {
        }

        return null
    }
}
/** Private constructor to prevent creating Icons instance.  */
