package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files

open class UnpackServerTask : DefaultTask() {

    @TaskAction
    fun unpackServer() {
        project.copy { copy ->
            with(copy) {
                val serverDirectory = project.buildDir.toPath().resolve(ServerStarterPlugin.SERVER_DIRECTORY_NAME)
                Files.list(serverDirectory).filter { Files.isRegularFile(it) }.forEach {
                    val lowerCaseFileName = it.fileName.toString().toLowerCase()
                    when {
                        lowerCaseFileName.endsWith(".zip") -> {
                            from(project.zipTree(it.toFile())).into(serverDirectory.toFile())
                        }
                        lowerCaseFileName.endsWith(".tar.gz") -> {
                            from(project.tarTree(it.toFile())).into(serverDirectory.toFile())
                        }
                    }
                }
            }
        }
    }
}