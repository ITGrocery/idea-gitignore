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

package mobi.hsz.idea.gitignore.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.OptionAction
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.ui.*
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import mobi.hsz.idea.gitignore.IgnoreBundle
import mobi.hsz.idea.gitignore.command.AppendFileCommandAction
import mobi.hsz.idea.gitignore.command.CreateFileCommandAction
import mobi.hsz.idea.gitignore.settings.IgnoreSettings
import mobi.hsz.idea.gitignore.ui.template.TemplateTreeComparator
import mobi.hsz.idea.gitignore.ui.template.TemplateTreeNode
import mobi.hsz.idea.gitignore.ui.template.TemplateTreeRenderer
import mobi.hsz.idea.gitignore.util.Constants
import mobi.hsz.idea.gitignore.util.Resources
import mobi.hsz.idea.gitignore.util.Resources.Template.Container.STARRED
import mobi.hsz.idea.gitignore.util.Resources.Template.Container.USER
import mobi.hsz.idea.gitignore.util.Utils
import org.jetbrains.annotations.NonNls
import java.awt.*
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

/**
 * [GeneratorDialog] responsible for displaying list of all available templates and adding selected ones
 * to the specified file.
 *
 * @author Jakub Chrzanowski <jakub></jakub>@hsz.mobi>
 * @since 0.2
 */
class GeneratorDialog
/**
 * Builds a new instance of [GeneratorDialog].
 *
 * @param project current working project
 * @param file    current working file
 */
(
        /** Current working project.  */
        private val project: Project, file: PsiFile?) : DialogWrapper(project, false) {

    /** Cache set to store checked templates for the current action.  */
    private val checked = ContainerUtil.newHashSet<Resources.Template>()

    /** Set of the starred templates.  */
    private val starred = ContainerUtil.newHashSet<String>()

    /** Settings instance.  */
    private val settings: IgnoreSettings

    /** Current working file.  */
    /**
     * Returns current file.
     *
     * @return file
     */
    var file: PsiFile? = null
        private set

    /** Templates tree root node.  */
    private val root: TemplateTreeNode

    /** [CreateFileCommandAction] action instance to generate new file in the proper time.  */
    private var action: CreateFileCommandAction? = null

    /** Templates tree with checkbox feature.  */
    private var tree: CheckboxTree? = null

    /** Tree expander responsible for expanding and collapsing tree structure.  */
    private var treeExpander: DefaultTreeExpander? = null

    /** Dynamic templates filter.  */
    private var profileFilter: FilterComponent? = null

    /** Preview editor with syntax highlight.  */
    private var preview: Editor? = null

    /** [Document] related to the [Editor] feature.  */
    private var previewDocument: Document? = null

    init {
        this.file = file
        this.root = TemplateTreeNode()
        this.action = null
        this.settings = IgnoreSettings.getInstance()

        title = IgnoreBundle.message("dialog.generator.title")
        setOKButtonText(IgnoreBundle.message("global.generate"))
        setCancelButtonText(IgnoreBundle.message("global.cancel"))
        init()
    }

    /**
     * Builds a new instance of [GeneratorDialog].
     *
     * @param project current working project
     * @param action  [CreateFileCommandAction] action instance to generate new file in the proper time
     */
    constructor(project: Project, action: CreateFileCommandAction?) : this(project, null as PsiFile?) {
        this.action = action
    }

    /**
     * Returns component which should be focused when the dialog appears on the screen.
     *
     * @return component to focus
     */
    override fun getPreferredFocusedComponent(): JComponent? = profileFilter

    /**
     * Dispose the wrapped and releases all resources allocated be the wrapper to help
     * more efficient garbage collection. You should never invoke this method twice or
     * invoke any method of the wrapper after invocation of `dispose`.
     *
     * @throws IllegalStateException if the dialog is disposed not on the event dispatch thread
     */
    override fun dispose() {
        EditorFactory.getInstance().releaseEditor(preview!!)
        super.dispose()
    }

    /**
     * Show the dialog.
     *
     * @throws IllegalStateException if the method is invoked not on the event dispatch thread
     * @see .showAndGet
     * @see .showAndGetOk
     */
    override fun show() {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            dispose()
            return
        }
        super.show()
    }

    /**
     * This method is invoked by default implementation of "OK" action. It just closes dialog
     * with `OK_EXIT_CODE`. This is convenient place to override functionality of "OK" action.
     * Note that the method does nothing if "OK" action isn't enabled.
     */
    override fun doOKAction() {
        if (isOKActionEnabled) {
            performAppendAction(false, false)
        }
    }

    /**
     * Performs [AppendFileCommandAction] action.
     *
     * @param ignoreDuplicates ignores duplicated rules
     * @param ignoreComments   ignores comments and empty lines
     */
    private fun performAppendAction(ignoreDuplicates: Boolean, ignoreComments: Boolean) {
        var content = ""
        for (template in checked) {
            if (template == null) {
                continue
            }
            content += IgnoreBundle.message("file.templateSection", template.name)
            content += Constants.NEWLINE + template.content!!
        }
        if (file == null && action != null) {
            file = action!!.execute().resultObject
        }
        if (file != null && !content.isEmpty()) {
            AppendFileCommandAction(project, file!!, content, ignoreDuplicates, ignoreComments).execute()
        }
        super.doOKAction()
    }

    /** Creates default actions with appended [OptionOkAction] instance.  */
    override fun createDefaultActions() {
        super.createDefaultActions()
        myOKAction = OptionOkAction()
    }

    /**
     * Factory method. It creates panel with dialog options. Options panel is located at the
     * center of the dialog's content pane. The implementation can return `null`
     * value. In this case there will be no options panel.
     *
     * @return center panel
     */
    override fun createCenterPanel(): JComponent? {
        // general panel
        val centerPanel = JPanel(BorderLayout())
        centerPanel.preferredSize = Dimension(800, 500)

        // splitter panel - contains tree panel and preview component
        val splitter = JBSplitter(false, 0.4f)
        centerPanel.add(splitter, BorderLayout.CENTER)

        val treePanel = JPanel(BorderLayout())
        previewDocument = EditorFactory.getInstance().createDocument("")
        preview = Utils.createPreviewEditor(previewDocument!!, project, true)

        splitter.firstComponent = treePanel
        splitter.secondComponent = preview!!.component

        /* Scroll panel for the templates tree. */
        val treeScrollPanel = createTreeScrollPanel()
        treePanel.add(treeScrollPanel, BorderLayout.CENTER)

        val northPanel = JPanel(GridBagLayout())
        northPanel.border = IdeBorderFactory.createEmptyBorder(2, 0, 2, 0)
        northPanel.add(createTreeActionsToolbarPanel(treeScrollPanel).component,
                GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE_LEADING,
                        GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0)
        )
        northPanel.add(profileFilter!!, GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE_TRAILING,
                GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        treePanel.add(northPanel, BorderLayout.NORTH)

        return centerPanel
    }

    /**
     * Creates scroll panel with templates tree in it.
     *
     * @return scroll panel
     */
    private fun createTreeScrollPanel(): JScrollPane {
        fillTreeData(null, true)

        val renderer = object : TemplateTreeRenderer() {
            override fun getFilter(): String? {
                return when {
                    profileFilter != null -> profileFilter!!.filter
                    else -> null
                }
            }
        }

        tree = object : CheckboxTree(renderer, root) {
            override fun getPreferredScrollableViewportSize(): Dimension {
                var size = super.getPreferredScrollableViewportSize()
                size = Dimension(size.width + 10, size.height)
                return size
            }

            override fun onNodeStateChanged(node: CheckedTreeNode?) {
                super.onNodeStateChanged(node)
                val template = (node as TemplateTreeNode).template
                when {
                    node.isChecked -> checked.add(template)
                    else -> checked.remove(template)
                }
            }
        }

        tree!!.cellRenderer = renderer
        tree!!.isRootVisible = false
        tree!!.showsRootHandles = true
        UIUtil.setLineStyleAngled(tree!!)
        TreeUtil.installActions(tree!!)

        tree!!.addTreeSelectionListener {
            val path = currentPath
            if (path != null) {
                updateDescriptionPanel(path)
            }
        }

        val scrollPane = ScrollPaneFactory.createScrollPane(tree)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        TreeUtil.expandAll(tree!!)

        treeExpander = DefaultTreeExpander(tree!!)
        profileFilter = TemplatesFilterComponent()

        return scrollPane
    }

    private val currentPath: TreePath?
        get() = when {
            tree!!.selectionPaths != null && tree!!.selectionPaths!!.size == 1 -> tree!!.selectionPaths!![0]
            else -> null
        }

    /**
     * Creates tree toolbar panel with actions for working with templates tree.
     *
     * @param target templates tree
     * @return action toolbar
     */
    private fun createTreeActionsToolbarPanel(target: JComponent): ActionToolbar {
        val actionManager = CommonActionsManager.getInstance()

        val actions = DefaultActionGroup()
        actions.add(actionManager.createExpandAllAction(treeExpander, tree))
        actions.add(actionManager.createCollapseAllAction(treeExpander, tree))
        actions.add(object : AnAction(
                IgnoreBundle.message("dialog.generator.unselectAll"), null,
                AllIcons.Actions.Unselectall) {
            override fun update(e: AnActionEvent?) {
                e!!.presentation.isEnabled = !checked.isEmpty()
            }

            override fun actionPerformed(e: AnActionEvent) {
                checked.clear()
                filterTree(profileFilter!!.textEditor.text)
            }
        })
        actions.add(object : AnAction(IgnoreBundle.message("dialog.generator.star"), null, STAR) {
            override fun update(e: AnActionEvent?) {
                val node = currentNode
                val disabled = node == null || USER == node.container || !node.isLeaf
                val unstar = node != null && STARRED == node.container

                val icon = when {
                    disabled -> IconLoader.getDisabledIcon(STAR)
                    unstar -> IconLoader.getTransparentIcon(STAR)
                    else -> STAR
                }
                val text = IgnoreBundle.message(if (unstar) "dialog.generator.unstar" else "dialog.generator.star")

                val presentation = e!!.presentation
                presentation.isEnabled = !disabled
                presentation.icon = icon
                presentation.text = text
            }

            override fun actionPerformed(e: AnActionEvent) {
                val node = currentNode ?: return

                val template = node.template
                if (template != null) {
                    val isStarred = !template.isStarred
                    template.isStarred = isStarred
                    refreshTree()

                    when {
                        isStarred -> starred.add(template.name)
                        else -> starred.remove(template.name)
                    }

                    settings.starredTemplates = ContainerUtil.newArrayList(starred)
                }
            }

            /**
             * Returns current [TemplateTreeNode] node if available.
             *
             * @return current node
             */
            private val currentNode: TemplateTreeNode?
                get() {
                    val path = currentPath
                    return when (path) {
                        null -> null
                        else -> path.lastPathComponent as TemplateTreeNode
                    }
                }
        })

        val actionToolbar = ActionManager.getInstance()
                .createActionToolbar(ActionPlaces.UNKNOWN, actions, true)
        actionToolbar.setTargetComponent(target)
        return actionToolbar
    }

    /**
     * Updates editor's content depending on the selected [TreePath].
     *
     * @param path selected tree path
     */
    private fun updateDescriptionPanel(path: TreePath) {
        val node = path.lastPathComponent as TemplateTreeNode
        val template = node.template

        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().runUndoTransparentAction {
                val content = when {
                    template != null -> StringUtil.replaceChar(StringUtil.notNullize(template.content), '\r', '\u0000')
                    else -> ""
                }
                previewDocument!!.replaceString(0, previewDocument!!.textLength, content)

                val pairs = getFilterRanges(profileFilter!!.textEditor.text, content)
                highlightWords(pairs)
            }
        }
    }

    /**
     * Fills templates tree with templates fetched with [Resources.getGitignoreTemplates].
     *
     * @param filter       templates filter
     * @param forceInclude force include
     */
    private fun fillTreeData(filter: String?, forceInclude: Boolean) {
        root.removeAllChildren()
        root.isChecked = false

        Resources.Template.Container.values().forEach { container ->
            val node = TemplateTreeNode(container)
            node.isChecked = false
            root.add(node)
        }

        val templatesList = Resources.getGitignoreTemplates()
        templatesList.forEach { template ->
            if (filter != null && filter.length > 0 && !isTemplateAccepted(template, filter)) {
                return@forEach
            }

            val node = TemplateTreeNode(template)
            node.isChecked = checked.contains(template)
            getGroupNode(root, template.container).add(node)
        }

        if (filter != null && forceInclude && root.childCount == 0) {
            fillTreeData(filter, false)
        }

        TreeUtil.sort(root, TemplateTreeComparator())
    }

    /**
     * Finds for the filter's words in the given content and returns their positions.
     *
     * @param filter  templates filter
     * @param content templates content
     * @return text ranges
     */
    private fun getFilterRanges(filter: String, content: String): List<Pair<Int, Int>> {
        var content = content
        val pairs = ContainerUtil.newArrayList<Pair<Int, Int>>()
        content = content.toLowerCase()

        Utils.getWords(filter).forEach { word ->
            var index = content.indexOf(word)
            while (index >= 0) {
                pairs.add(Pair.create(index, index + word.length))
                index = content.indexOf(word, index + 1)
            }
        }

        return pairs
    }

    /**
     * Checks if given template is accepted by passed filter.
     *
     * @param template to check
     * @param filter   templates filter
     * @return template is accepted
     */
    private fun isTemplateAccepted(template: Resources.Template, filter: String): Boolean {
        var filter = filter
        filter = filter.toLowerCase()

        if (StringUtil.containsIgnoreCase(template.name, filter)) {
            return true
        }

        val nameAccepted = Utils.getWords(filter).any { StringUtil.containsIgnoreCase(template.name, it) }

        val ranges = getFilterRanges(filter, StringUtil.notNullize(template.content))
        return nameAccepted || ranges.isNotEmpty()
    }

    /**
     * Filters templates tree.
     *
     * @param filter text
     */
    private fun filterTree(filter: String?) {
        if (tree != null) {
            fillTreeData(filter, true)
            reloadModel()
            TreeUtil.expandAll(tree!!)
            if (tree!!.selectionPath == null) {
                TreeUtil.selectFirstNode(tree!!)
            }
        }
    }

    /** Refreshes current tree.  */
    private fun refreshTree() {
        filterTree(profileFilter!!.textEditor.text)
    }

    /**
     * Highlights given text ranges in [.preview] content.
     *
     * @param pairs text ranges
     */
    private fun highlightWords(pairs: List<Pair<Int, Int>>) {
        val attr = TextAttributes()
        attr.backgroundColor = UIUtil.getTreeSelectionBackground()
        attr.foregroundColor = UIUtil.getTreeSelectionForeground()

        for (pair in pairs) {
            preview!!.markupModel.addRangeHighlighter(pair.first, pair.second, 0, attr,
                    HighlighterTargetArea.EXACT_RANGE)
        }
    }

    /** Reloads tree model.  */
    private fun reloadModel() {
        (tree!!.model as DefaultTreeModel).reload()
    }

    /** Custom templates [FilterComponent].  */
    /** Builds a new instance of [TemplatesFilterComponent].  */
    private inner class TemplatesFilterComponent : FilterComponent(TEMPLATES_FILTER_HISTORY, 10) {

        /** Filters tree using current filter's value.  */
        override fun filter() {
            filterTree(filter)
        }
    }

    /** [OkAction] instance with additional `Generate without duplicates` action.  */
    private inner class OptionOkAction : DialogWrapper.OkAction(), OptionAction {
        override fun getOptions(): Array<Action> {
            return arrayOf(object : DialogWrapper.DialogWrapperAction(IgnoreBundle.message("global.generate.without.duplicates")) {
                override fun doAction(e: ActionEvent) {
                    performAppendAction(true, false)
                }
            }, object : DialogWrapper.DialogWrapperAction(IgnoreBundle.message("global.generate.without.comments")) {
                override fun doAction(e: ActionEvent) {
                    performAppendAction(false, true)
                }
            })
        }
    }

    companion object {
        /** [FilterComponent] search history key.  */
        @NonNls
        private val TEMPLATES_FILTER_HISTORY = "TEMPLATES_FILTER_HISTORY"

        /** Star icon for the favorites action.  */
        private val STAR = AllIcons.Ide.Rating

        /**
         * Creates or gets existing group node for specified element.
         *
         * @param root      tree root node
         * @param container container type to search
         * @return group node
         */
        private fun getGroupNode(root: TemplateTreeNode,
                                 container: Resources.Template.Container): TemplateTreeNode {
            (0 until root.childCount)
                    .map { root.getChildAt(it) as TemplateTreeNode }
                    .filter { container == it.container }
                    .forEach { return it }

            val child = TemplateTreeNode(container)
            root.add(child)
            return child
        }
    }
}
