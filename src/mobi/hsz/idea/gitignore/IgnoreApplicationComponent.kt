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

package mobi.hsz.idea.gitignore

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import mobi.hsz.idea.gitignore.settings.IgnoreSettings
import mobi.hsz.idea.gitignore.util.Utils

/**
 * @author Jakub Chrzanowski <jakub@hsz.mobi>
 * @since 1.3
 */
class IgnoreApplicationComponent : ApplicationComponent {
    /** Plugin has been updated with the current run. */
    var updated: Boolean = false
        private set

    /** Plugin update notification has been shown. */
    var notificationShown: Boolean = false

    /** Component initialization method. */
    override fun initComponent() {
        /* The settings storage object. */
        val settings = IgnoreSettings.getInstance()
        updated = Utils.getVersion() != settings.version
        if (updated) {
            settings.version = Utils.getVersion()
        }
    }

    /** Component dispose method. */
    override fun disposeComponent() {}

    /**
     * Returns component's name.
     *
     * @return component's name
     */
    override fun getComponentName(): String = "IgnoreApplicationComponent"

    companion object {
        /**
         * Get Ignore Application Component
         *
         * @return Ignore Application Component
         */
        val instance: IgnoreApplicationComponent
            get() = ApplicationManager.getApplication().getComponent(IgnoreApplicationComponent::class.java)
    }
}
