package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import com.google.common.io.Resources
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.os.OperatingSystem
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors.joining

open class ConfigureServerTask : DefaultTask() {

    private val serverDirectory: Path by lazy {
        val serverDirectoryBase = project.buildDir.toPath().resolve(ServerStarterPlugin.SERVER_DIRECTORY_NAME)
        if (OperatingSystem.current().isLinux) {
            serverDirectoryBase.resolve("samp03")
        } else {
            serverDirectoryBase
        }
    }
    private val gameModesDirectory = serverDirectory.resolve("gamemodes")
    private val kampAmxFile = gameModesDirectory.resolve("kamp.amx")
    private val kampDirectory = serverDirectory.resolve("Kamp")
    private val dataDirectory = kampDirectory.resolve("data")
    private val launchDirectory = kampDirectory.resolve("launch")
    private val jarsDirectory = launchDirectory.resolve("jars")
    private val pluginsDirectory = serverDirectory.resolve("plugins")
    private val serverCfgFile: Path = serverDirectory.resolve("server.cfg")
    private val jvmOptsFile = launchDirectory.resolve("jvmopts.txt")
    private val configPropertiesFile = kampDirectory.resolve("config.properties")
    private val kampPluginBinaryFile: Path
        get() = Paths.get(extension.kampPluginBinaryPath ?: throw IllegalStateException("kampPluginBinaryPath was not set"))

    private val extension: ServerStarterPluginExtension
        get() = project.extensions.getByType(ServerStarterPluginExtension::class.java)

    private val runtimeConfiguration: Configuration
        get() = project.configurations.getByName("runtime")

    private val jarTask: Jar
        get() = (project.tasks.getByName("jar") as Jar)

    @InputFiles
    fun getInputFiles(): List<File> {
        val inputFiles: MutableList<File> = mutableListOf()
        inputFiles += serverCfgFile.toFile()
        inputFiles += runtimeConfiguration.resolve()
        inputFiles += jarTask.archivePath
        return inputFiles
    }

    @OutputFiles
    fun getOutputFiles(): List<File> {
        val outputFiles: MutableList<File> = mutableListOf()
        outputFiles += serverCfgFile.toFile()
        outputFiles += kampPluginBinaryFile.toFile()
        outputFiles += kampAmxFile.toFile()
        return outputFiles
    }

    @OutputDirectory
    fun getOutputDirectory(): File = kampDirectory.toFile()

    @TaskAction
    fun configureServer() {
        createDirectories()
        copyDependencies()
        writeServerCfg()
        createJvmOptsFile()
        createConfigPropertiesFile()
        copyPluginBinaryFile()
        copyPawnScript()
    }

    private fun createDirectories() {
        Files.createDirectories(kampDirectory)
        Files.createDirectories(dataDirectory)
        Files.createDirectories(launchDirectory)
        Files.createDirectories(jarsDirectory)
        Files.createDirectories(pluginsDirectory)
    }

    private fun copyDependencies() {
        // Need to delete old jars in case they're outdated regarding version
        Files.list(jarsDirectory).filter { Files.isRegularFile(it) }.forEach { Files.delete(it) }
        runtimeConfiguration.resolve().forEach {
            val jarFile = it.toPath()
            Files.copy(jarFile, jarsDirectory.resolve(jarFile.fileName))
        }
        val archivePath = jarTask.archivePath.toPath()
        Files.copy(archivePath, jarsDirectory.resolve(archivePath.fileName))
    }

    private fun writeServerCfg() {
        Files.newBufferedWriter(serverCfgFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE).use { writer ->
            with(writer) {
                write("echo Executing Server Config...\n")
                write("lanmode ${extension.lanMode.toInt()}\n")
                write("rcon_password ${extension.rconPassword}\n")
                write("maxplayers ${extension.maxPlayers}\n")
                write("port ${extension.port}\n")
                write("hostname ${extension.hostName}\n")
                write("gamemode0 kamp 1\n")
                val pluginFileName = kampPluginBinaryFile.fileName.toString()
                when {
                    OperatingSystem.current().isWindows -> {
                        val pluginName = pluginFileName.replace(".dll", "", ignoreCase = true)
                        write("plugins $pluginName\n")
                    }
                    OperatingSystem.current().isLinux -> write("plugins $pluginFileName\n")
                    else -> throw UnsupportedOperationException("Unsupported operating system: ${OperatingSystem.current()}")
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
        Files.newBufferedWriter(configPropertiesFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE).use { writer ->
            with(writer) {
                val gameModeClassName = extension.gameModeClassName ?: throw IllegalStateException("gameModeClassName was not set")
                write("kamp.gamemode.class.name=$gameModeClassName\n")
                val pluginName = kampPluginBinaryFile.fileName.toString().replace(".dll", "", ignoreCase = true)
                write("kamp.plugin.name=$pluginName\n")
                extension.configProperties.forEach { key, value ->
                    write("$key=$value\n")
                }
            }
        }
    }

    private fun createJvmOptsFile() {
        Files.newBufferedWriter(jvmOptsFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE).use { writer ->
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
            Files
                    .list(jarsDirectory)
                    .filter { Files.isRegularFile(it) }
                    .map { serverDirectory.relativize(it).toString() }
                    .filter { it.endsWith(".jar", ignoreCase = true) }
                    .collect(joining(";"))

    private fun copyPluginBinaryFile() {
        Files.copy(kampPluginBinaryFile, pluginsDirectory.resolve(kampPluginBinaryFile.fileName), StandardCopyOption.REPLACE_EXISTING)
    }

    private fun copyPawnScript() {
        val kampAmx = Resources.toByteArray(Resources.getResource(this::class.java, "kamp.amx"))
        Files.newOutputStream(kampAmxFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use {
            it.write(kampAmx)
        }
    }

}