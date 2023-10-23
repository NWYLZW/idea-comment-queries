package yij.ie.ideacommentqueries

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.JCheckBox

open class Config(private val project: Project) : Configurable {
    override fun getDisplayName() = "Comment Queries"

    private val service = ConfigService.getInstance(project)

    private val disableCheckBox = JCheckBox("Disable for this project", service.disable)
    private val panel = FormBuilder
        .createFormBuilder()
        .addComponent(disableCheckBox, 1)
        .addComponentToRightColumn(object : JBLabel(
            """
            Disable comment queries for this project, and you can use `/* comment-queries-enable */` to enable it for a file.
            """.trimIndent(), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
        ) {
            init { border = JBUI.Borders.emptyLeft(13) }
        }, 0)
        .addComponentToRightColumn(object : JBLabel(
            """
            If you want to disable it for a file, you can use `/* comment-queries-disable */`.
            """.trimIndent(), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
        ) {
            init { border = JBUI.Borders.emptyLeft(13) }
        }, 0)
        .addComponentFillVertically(JBLabel(""), 0)
        .panel

    override fun createComponent() = panel!!

    override fun isModified(): Boolean {
        var modified = false
        modified = modified || disableCheckBox.isSelected != service.disable
        return modified
    }

    override fun apply() {
        service.disable = disableCheckBox.isSelected
    }

    override fun reset() {
        disableCheckBox.isSelected = service.disable
    }
}