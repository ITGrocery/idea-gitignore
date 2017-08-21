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

import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.project.Project
import mobi.hsz.idea.gitignore.util.Notify

/**
 * ProjectComponent instance to display plugin's update information.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.3
 */
class IgnoreUpdateComponent

/**
 * Constructor.
 *
 * @param project current project
 */
private constructor(project: Project) : AbstractProjectComponent(project) {
    /** [IgnoreApplicationComponent] instance. */
    private var application: IgnoreApplicationComponent? = null

    /** Component initialization method. */
    override fun initComponent() {
        application = IgnoreApplicationComponent.getInstance()
    }

    /** Component dispose method. */
    override fun disposeComponent() {
        application = null
    }

    /**
     * Returns component's name.
     *
     * @return component's name
     */
    override fun getComponentName(): String = "IgnoreUpdateComponent"

    /** Method called when project is opened. */
    override fun projectOpened() {
        if (application!!.isUpdated && !application!!.isUpdateNotificationShown) {
            application!!.isUpdateNotificationShown = true
            Notify.showUpdate(myProject)
        }
    }
}
