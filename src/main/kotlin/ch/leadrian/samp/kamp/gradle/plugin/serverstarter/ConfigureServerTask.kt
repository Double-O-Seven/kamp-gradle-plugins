package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import com.google.common.io.Resources
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

open class ConfigureServerTask : DefaultTask() {

    private val extension: ServerStarterPluginExtension
        get() = project.extensions.getByType(ServerStarterPluginExtension::class.java)

    private val serverDirectory: File by lazy {
        val serverDirectoryBase = project.buildDir.resolve(ServerStarterPlugin.SERVER_DIRECTORY_NAME)
        if (extension.operatingSystem.isLinux) {
            serverDirectoryBase.resolve("samp03")
        } else {
            serverDirectoryBase
        }
    }

    private val gamemodesDirectory = serverDirectory.resolve("gamemodes")

    private val kampAmxFile = gamemodesDirectory.resolve("kamp.amx")

    private val kampDirectory = serverDirectory.resolve("Kamp")

    private val dataDirectory = kampDirectory.resolve("data")

    private val launchDirectory = kampDirectory.resolve("launch")

    private val jarsDirectory = launchDirectory.resolve("jars")

    private val pluginsDirectory = serverDirectory.resolve("plugins")

    private val serverCfgFile = serverDirectory.resolve("server.cfg")

    private val jvmoptsTxtFile = launchDirectory.resolve("jvmopts.txt")

    private val configPropertiesFile = kampDirectory.resolve("config.properties")

    private val kampPluginFileName: String
        get() {
            val os = extension.operatingSystem
            return when {
                os.isWindows -> "kamp.dll"
                os.isLinux -> "libkamp.so"
                else -> throw UnsupportedOperationException("Unsupported operating system: $os")
            }
        }

    private val kampPluginFile = pluginsDirectory.resolve(kampPluginFileName)

    private val sampgdkFileName: String
        get() {
            val os = extension.operatingSystem
            return when {
                os.isWindows -> "sampgdk4.dll"
                os.isLinux -> "libsampgdk.so"
                else -> throw UnsupportedOperationException("Unsupported operating system: $os")
            }
        }

    private val sampgdkFile = serverDirectory.resolve(sampgdkFileName)

    private val runtimeConfiguration: Configuration
        get() = project.configurations.getByName("runtimeClasspath")

    private val jarFiles: List<File>
        get() = project.tasks.withType(Jar::class.java).mapNotNull { it.archiveFile.orNull?.asFile }

    @InputFiles
    fun getInputFiles(): List<File> {
        val inputFiles: MutableList<File> = mutableListOf()
        inputFiles += serverCfgFile
        inputFiles += runtimeConfiguration.resolve()
        jarFiles.forEach { inputFiles += it }
        return inputFiles
    }

    @OutputFiles
    fun getOutputFiles(): List<File> {
        val outputFiles: MutableList<File> = mutableListOf()
        outputFiles += serverCfgFile
        outputFiles += kampPluginFile
        outputFiles += sampgdkFile
        outputFiles += kampAmxFile
        return outputFiles
    }

    @OutputDirectory
    fun getOutputDirectory(): File = kampDirectory

    @TaskAction
    fun configureServer() {
        createDirectories()
        copyDependencies()
        writeServerCfg()
        createJvmOptsFile()
        createConfigPropertiesFile()
        copyKampPluginFile()
        copySampgdkFile()
        copyKampAmxFile()
    }

    private fun createDirectories() {
        kampDirectory.mkdirs()
        dataDirectory.mkdirs()
        launchDirectory.mkdirs()
        jarsDirectory.mkdirs()
        pluginsDirectory.mkdirs()
    }

    private fun copyDependencies() {
        // Need to delete old jars in case they're outdated regarding version
        jarsDirectory.listFiles().filter { it.isFile }.forEach {
            it.delete()
        }
        runtimeConfiguration.resolve().forEach {
            it.copyTo(jarsDirectory.resolve(it.name))
        }
        jarFiles.forEach { jarFile ->
            jarFile.copyTo(jarsDirectory.resolve(jarFile.name))
        }
    }

    private fun writeServerCfg() {
        FileWriter(serverCfgFile).use { writer ->
            with(writer) {
                write("echo Executing Server Config...\n")
                write("lanmode ${extension.lanMode.toInt()}\n")
                write("rcon_password ${extension.rconPassword}\n")
                write("maxplayers ${extension.maxPlayers}\n")
                write("port ${extension.port}\n")
                write("hostname ${extension.hostName}\n")
                write("gamemode0 kamp 1\n")
                when {
                    extension.operatingSystem.isWindows -> {
                        val pluginName = kampPluginFileName.replace(".dll", "", ignoreCase = true)
                        write("plugins $pluginName\n")
                    }
                    else -> write("plugins $kampPluginFileName\n")
                }
                write("announce ${extension.announce.toInt()}\n")
                write("chatlogging ${extension.chatLogging.toInt()}\n")
                write("weburl ${extension.webUrl}\n")
                write("onfoot_rate ${extension.onFootRate}\n")
                write("incar_rate ${extension.inCarRate}\n")
                write("weapon_rate ${extension.weaponRate}\n")
                write("stream_distance ${extension.streamDistance}\n")
                write("stream_rate ${extension.streamRate}\n")
                write("maxnpc ${extension.maxNPCs}\n")
                write("logtimeformat ${extension.logTimeFormat}\n")
                write("language ${extension.language}\n")
            }
        }
    }

    private fun Boolean.toInt(): Int = if (this) 1 else 0

    private fun createConfigPropertiesFile() {
        FileWriter(configPropertiesFile).use { writer ->
            with(writer) {
                val gameModeClassName = extension.gameModeClassName ?: throw IllegalStateException("gameModeClassName was not set")
                write("kamp.gamemode.class.name=$gameModeClassName\n")
                val pluginName = kampPluginFileName.replace(".dll", "", ignoreCase = true)
                write("kamp.plugin.name=$pluginName\n")
                extension.configProperties.forEach { key, value ->
                    write("$key=$value\n")
                }
            }
        }
    }

    private fun createJvmOptsFile() {
        FileWriter(jvmoptsTxtFile).use { writer ->
            with(writer) {
                val classPath = buildClassPath()
                write("-Djava.class.path=$classPath\n")
                extension.jvmOptions.forEach {
                    write(it)
                    write("\n")
                }
            }
        }
    }

    private fun buildClassPath(): String =
            jarsDirectory
                    .listFiles()
                    .filter { it.isFile }
                    .map { it.relativeTo(serverDirectory).toString() }
                    .filter { it.endsWith(".jar", ignoreCase = true) }
                    .joinToString(File.pathSeparator)

    private fun copyKampPluginFile() {
        copyResource("lib/${extension.operatingSystem.familyName}/$kampPluginFileName", kampPluginFile)
    }

    private fun copySampgdkFile() {
        copyResource("lib/${extension.operatingSystem.familyName}/$sampgdkFileName", sampgdkFile)
    }

    private fun copyKampAmxFile() {
        copyResource("kamp.amx", kampAmxFile)
    }

    private fun copyResource(resourceName: String, destination: File) {
        FileOutputStream(destination).use { outputStream ->
            Resources.copy(javaClass.getResource(resourceName), outputStream)
        }
    }

}