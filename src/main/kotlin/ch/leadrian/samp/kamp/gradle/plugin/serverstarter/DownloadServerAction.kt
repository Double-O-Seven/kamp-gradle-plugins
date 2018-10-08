package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Action
import org.gradle.internal.os.OperatingSystem
import java.nio.file.Files

open class DownloadServerAction : Action<Download> {

    override fun execute(download: Download) {
        with(download) {
            val extension = project.extensions.getByType(ServerStarterPluginExtension::class.java)
            val downloadUrl = when {
                OperatingSystem.current().isWindows -> extension.windowsServerDownloadUrl
                else -> extension.linuxServerDownloadUrl
            }
            val serverDirectory = project.buildDir.toPath().resolve(ServerStarterPlugin.SERVER_DIRECTORY_NAME)
            Files.createDirectories(serverDirectory)
            src(downloadUrl)
            dest(serverDirectory.toFile())
            overwrite(false)
        }
    }
}