package ch.leadrian.samp.kamp.gradle.plugin.textkeygen

import org.gradle.api.NonNullApi
import org.gradle.api.Plugin
import org.gradle.api.Project

@NonNullApi
open class TextKeysGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("textKeyGenerator", TextKeysGeneratorPluginExtension::class.java)
        project.tasks.create("generateTextKeys", GenerateTextKeysTask::class.java)
    }
}
