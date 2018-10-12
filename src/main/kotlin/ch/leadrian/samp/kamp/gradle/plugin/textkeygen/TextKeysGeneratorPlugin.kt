package ch.leadrian.samp.kamp.gradle.plugin.textkeygen

import org.gradle.api.NonNullApi
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@NonNullApi
open class TextKeysGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("textKeyGenerator", TextKeysGeneratorPluginExtension::class.java)
        val generateTextKeysTask = project.tasks.create("generateTextKeys", GenerateTextKeysTask::class.java)
        project.tasks.withType(JavaCompile::class.java) { it.dependsOn(generateTextKeysTask) }
        project.tasks.withType(KotlinCompile::class.java) { it.dependsOn(generateTextKeysTask) }
    }
}
