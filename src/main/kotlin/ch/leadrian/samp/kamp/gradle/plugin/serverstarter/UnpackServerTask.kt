package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path

open class UnpackServerTask : DefaultTask() {

    private val extension: ServerStarterPluginExtension
        get() = project.extensions.getByType(ServerStarterPluginExtension::class.java)

    private val serverDirectory = project.buildDir.toPath().resolve(ServerStarterPlugin.SERVER_DIRECTORY_NAME)

    private val serverDownloadFile: Path
        get() = serverDirectory.resolve(extension.downloadFileName)

    @InputFile
    fun getInputFile(): File = serverDownloadFile.toFile()

    @OutputDirectory
    fun getOutputDirectory(): File = serverDirectory.toFile()

    @TaskAction
    fun unpackServer() {
        project.copy { copy ->
            val archive = serverDownloadFile.let {
                when {
                    it.isZipFile() -> project.zipTree(it.toFile())
                    it.isTarFile() -> project.tarTree(it.toFile())
                    else -> throw UnsupportedOperationException("Unsupported archive: $it")
                }
            }
            copy.from(archive).into(serverDirectory.toFile())
        }
    }

    private fun Path.isZipFile(): Boolean = fileNameEndsWith(".zip")

    private fun Path.isTarFile(): Boolean = fileNameEndsWith(".tar.gz")

    private fun Path.fileNameEndsWith(fileEnding: String) = fileName.toString().endsWith(fileEnding, ignoreCase = true)
}
