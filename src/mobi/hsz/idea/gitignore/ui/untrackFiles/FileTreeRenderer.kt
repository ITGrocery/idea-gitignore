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

package mobi.hsz.idea.gitignore.ui.untrackFiles

import com.intellij.openapi.util.Iconable
import com.intellij.ui.CheckboxTree
import com.intellij.util.IconUtil
import javax.swing.JTree

/**
 * [FileTreeRenderer] implementation of checkbox renderer.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 1.7
 */
class FileTreeRenderer : CheckboxTree.CheckboxTreeCellRenderer() {
    /**
     * Renders checkbox tree cell filled with @{link [FileTreeNode] data.
     *
     * @param tree     current working tree
     * @param value    template data
     * @param selected node is selected
     * @param expanded node is expanded
     * @param leaf     node is a leaf
     * @param row      node is a row
     * @param hasFocus node has focus
     */
    override fun customizeRenderer(tree: JTree, value: Any?, selected: Boolean,
                                   expanded: Boolean, leaf: Boolean, row: Int,
                                   hasFocus: Boolean) {
        if (value !is FileTreeNode) {
            return
        }

        val node = value as FileTreeNode?
        val file = node!!.file
        val project = node.project

        textRenderer.append(file.name)
        textRenderer.icon = IconUtil.getIcon(file, Iconable.ICON_FLAG_READ_STATUS, project)
    }
}
