package yij.ie.ideacommentqueries

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JCheckBox

open class Config(private val project: Project) : Configurable {
    override fun getDisplayName() = "Comment Queries"

    private val service = ConfigService.getInstance(project)

    private val disableCheckBox = JCheckBox("Disable for this project")
    private val panel = FormBuilder
        .createFormBuilder()
        .addComponent(disableCheckBox, 1)
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