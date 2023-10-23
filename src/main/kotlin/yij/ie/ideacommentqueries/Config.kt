package yij.ie.ideacommentqueries

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.JCheckBox
import javax.swing.JTextField

open class Config(private val project: Project) : Configurable {
    override fun getDisplayName() = "Comment Queries"

    private val service = ConfigService.getInstance(project)

    private val disableCheckBox = JCheckBox("Disable for this project", service.disable)
    private val overflowLengthTextField = JTextField(service.overflowLength.toString())
    private val panel = FormBuilder
        .createFormBuilder()
        .addLabeledComponent("Status", disableCheckBox, 1, false)
        .addComponentToRightColumn(object : JBLabel(
            """
            Disable comment queries for this project, and you can use `/* comment-queries-enable */` to enable it for a file.
            """.trimIndent(), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
        ) {
            init { border = JBUI.Borders.emptyLeft(2) }
        }, 0)
        .addComponentToRightColumn(object : JBLabel(
            """
            If you want to disable it for a file, you can use `/* comment-queries-disable */`.
            """.trimIndent(), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
        ) {
            init { border = JBUI.Borders.emptyLeft(2) }
        }, 0)
        .addLabeledComponent("Overflow length", overflowLengthTextField, 1, false)
        .addComponentToRightColumn(object : JBLabel(
            """
            If query result is too long, it will be truncated to this length.
            """.trimIndent(), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
        ) {
            init { border = JBUI.Borders.emptyLeft(2) }
        }, 0)
        .addComponentToRightColumn(object : JBLabel(
            """
            If you want to disable truncation, you can set this to 0.
            """.trimIndent(), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
        ) {
            init { border = JBUI.Borders.emptyLeft(2) }
        }, 0)
        .addComponentFillVertically(JBLabel(""), 0)
        .panel

    override fun createComponent() = panel!!

    override fun isModified(): Boolean {
        var modified = false
        modified = modified || disableCheckBox.isSelected != service.disable
        modified = modified || overflowLengthTextField.text.toInt() != service.overflowLength
        return modified
    }

    override fun apply() {
        service.disable = disableCheckBox.isSelected
        service.overflowLength = overflowLengthTextField.text.toInt()
    }

    override fun reset() {
        disableCheckBox.isSelected = service.disable
        overflowLengthTextField.text = service.overflowLength.toString()
    }
}