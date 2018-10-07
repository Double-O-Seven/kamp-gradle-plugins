package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project

open class ServerStarterPlugin : Plugin<Project> {

    companion object {

        const val SERVER_DIRECTORY_NAME = "samp-server"
    }

    override fun apply(project: Project) {
        project.extensions.create("serverStarter", ServerStarterPluginExtension::class.java)
        val downloadTasks = project.tasks.create("downloadServer", Download::class.java, DownloadServerAction())
        val unpackTask = project.tasks.create("unpackServer", UnpackServerTask::class.java).dependsOn(downloadTasks)
        project.tasks.create("configureServer", ConfigureServerTask::class.java).dependsOn(unpackTask)
    }

}