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

package mobi.hsz.idea.gitignore.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.vcs.VcsConfigurableProvider
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.ui.IgnoreSettingsPanel
import mobi.hsz.idea.gitignore.util.Utils
import javax.swing.JComponent

/**
 * Configuration interface for [IgnoreSettings].
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 0.6.1
 */
class IgnoreSettingsConfigurable : SearchableConfigurable, VcsConfigurableProvider {
    /** The settings storage object.  */
    private val settings: IgnoreSettings = IgnoreSettings.getInstance()

    /** The settings UI form.  */
    private var settingsPanel: IgnoreSettingsPanel? = null

    /**
     * Returns the user-visible name of the settings component.
     *
     * @return the visible name of the component [IgnoreSettingsConfigurable]
     */
    override fun getDisplayName(): String = IgnoreBundle.message("settings.displayName")

    /**
     * Returns the topic in the help file which is shown when help for the configurable is requested.
     *
     * @return the help topic, or null if no help is available [.getDisplayName]
     */
    override fun getHelpTopic(): String = displayName

    /**
     * Returns the user interface component for editing the configuration.
     *
     * @return the [IgnoreSettingsPanel] component instance
     */
    override fun createComponent(): JComponent? {
        if (settingsPanel == null) {
            settingsPanel = IgnoreSettingsPanel()
        }
        reset()
        return settingsPanel!!.panel
    }

    /**
     * Checks if the settings in the user interface component were modified by the user and need to be saved.
     *
     * @return true if the settings were modified, false otherwise.
     */
    override fun isModified(): Boolean = settingsPanel == null
            || !Comparing.equal(settings.isMissingGitignore, settingsPanel!!.isMissingGitignore)
            || !Utils.equalLists(settings.userTemplates, settingsPanel!!.userTemplates)
            || !Comparing.equal(settings.isIgnoredFileStatus, settingsPanel!!.isIgnoredFileStatus)
            || !Comparing.equal(settings.isOuterIgnoreRules, settingsPanel!!.isOuterIgnoreRules)
            || !Comparing.equal(settings.isInsertAtCursor, settingsPanel!!.isInsertAtCursor)
            || !Comparing.equal(settings.isAddUnversionedFiles, settingsPanel!!.isAddUnversionedFiles)
            || !Comparing.equal(settings.isUnignoreActions, settingsPanel!!.isUnignoreActions)
            || !Comparing.equal(settings.isInformTrackedIgnored, settingsPanel!!.isInformTrackedIgnored)
            || !Comparing.equal(settings.isNotifyIgnoredEditing, settingsPanel!!.isNotifyIgnoredEditing)
            || !settingsPanel!!.languagesSettings.equalSettings(settings.languagesSettings)

    /** Store the settings from configurable to other components.  */
    @Throws(ConfigurationException::class)
    override fun apply() {
        if (settingsPanel == null) {
            return
        }
        settings.isMissingGitignore = settingsPanel!!.isMissingGitignore
        settings.userTemplates = settingsPanel!!.userTemplates
        settings.isIgnoredFileStatus = settingsPanel!!.isIgnoredFileStatus
        settings.isOuterIgnoreRules = settingsPanel!!.isOuterIgnoreRules
        settings.isInsertAtCursor = settingsPanel!!.isInsertAtCursor
        settings.isAddUnversionedFiles = settingsPanel!!.isAddUnversionedFiles
        settings.languagesSettings = settingsPanel!!.languagesSettings.settings
        settings.isUnignoreActions = settingsPanel!!.isUnignoreActions
        settings.isInformTrackedIgnored = settingsPanel!!.isInformTrackedIgnored
        settings.isNotifyIgnoredEditing = settingsPanel!!.isNotifyIgnoredEditing
    }

    /** Load settings from other components to configurable.  */
    override fun reset() {
        if (settingsPanel == null) {
            return
        }
        settingsPanel!!.isMissingGitignore = settings.isMissingGitignore
        settingsPanel!!.userTemplates = settings.userTemplates
        settingsPanel!!.isIgnoredFileStatus = settings.isIgnoredFileStatus
        settingsPanel!!.isOuterIgnoreRules = settings.isOuterIgnoreRules
        settingsPanel!!.isInsertAtCursor = settings.isInsertAtCursor
        settingsPanel!!.isAddUnversionedFiles = settings.isAddUnversionedFiles
        settingsPanel!!.isUnignoreActions = settings.isUnignoreActions
        settingsPanel!!.isInformTrackedIgnored = settings.isInformTrackedIgnored
        settingsPanel!!.isNotifyIgnoredEditing = settings.isNotifyIgnoredEditing

        val model = settingsPanel!!.languagesSettings
        model.update(settings.languagesSettings.clone())
    }

    /** Disposes the Swing components used for displaying the configuration.  */
    override fun disposeUIResources() {
        settingsPanel!!.dispose()
        settingsPanel = null
    }

    /**
     * Returns current [Configurable] instance.
     *
     * @param project ignored
     * @return current instance
     */
    override fun getConfigurable(project: Project): Configurable? = this

    /**
     * Returns help topic as an ID.
     *
     * @return id
     *
     * @see {@link .getHelpTopic
     */
    override fun getId(): String = helpTopic

    /**
     * An action to perform when this configurable is opened.
     *
     * @param option setting search query
     * @return null
     */
    override fun enableSearch(option: String?): Runnable? = null
}
