package yij.ie.ideacommentqueries

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.PROJECT)
@State(name = "CommentQueriesConfigService", storages = [Storage("comment-queries.xml")])
class ConfigService : PersistentStateComponent<ConfigService> {
    var disable: Boolean = false
    var overflowLength: Int = 120
    override fun getState(): ConfigService {
        return this
    }
    override fun loadState(config: ConfigService) {
        XmlSerializerUtil.copyBean(config, this)
    }

    companion object {
        fun getInstance(project: Project): ConfigService {
            return project.getService(ConfigService::class.java)
        }
    }
}
