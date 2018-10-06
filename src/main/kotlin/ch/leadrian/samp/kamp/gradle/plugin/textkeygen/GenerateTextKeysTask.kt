package ch.leadrian.samp.kamp.gradle.plugin.textkeygen

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.Objects.requireNonNull
import java.util.regex.Pattern.compile
import kotlin.streams.toList

open class GenerateTextKeysTask : DefaultTask() {

    companion object {

        private val STRINGS_FILE_PATTERN = compile("strings(_[a-z]{2}(_[A-Z]{2})?)?\\.properties")
    }

    @InputFiles
    fun getInputFiles(): List<File> {
        val extension = extension
        val resourcesDirectoryFile = getFileFromObject(extension.resourcesDirectory)
        return extension.packageNames.flatMap { packageName -> getStringsPropertiesFiles(resourcesDirectoryFile, packageName) }
    }

    @OutputDirectories
    fun getOutputDirectories(): List<File> = extension.packageNames.map { this.getOutputDirectory(it) }

    private val extension: TextKeysGeneratorPluginExtension
        get() = project.extensions.getByType(TextKeysGeneratorPluginExtension::class.java)

    init {
        doLast { generateTextKeys() }
    }

    private fun generateTextKeys() {
        val extension = extension
        val resourcesDirectoryFile = getFileFromObject(extension.resourcesDirectory)
        extension.packageNames.forEach { packageName ->
            val stringsPropertiesFiles = getStringsPropertiesFiles(resourcesDirectoryFile, packageName)
            try {
                generateTextKeys(packageName, stringsPropertiesFiles)
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }
    }

    @Throws(IOException::class)
    private fun generateTextKeys(packageName: String, stringsPropertiesFiles: List<File>) {
        val stringPropertyNames = stringsPropertiesFiles
                .map { it.toPath() }
                .map { this.loadProperties(it) }
                .flatMap { it.stringPropertyNames() }
                .toSet()
        val textKeysGenerator = TextKeysGenerator()
        val outputDirectory = getOutputDirectory(packageName).toPath()

        Files.createDirectories(outputDirectory)
        Files.newBufferedWriter(outputDirectory.resolve("TextKeys.java"), StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { writer -> textKeysGenerator.generateTextKeyClasses("TextKeys", packageName, stringPropertyNames, writer) }
    }

    private fun getStringsPropertiesFiles(resourcesDirectoryFile: File, packageName: String): List<File> {
        val packagePath = packageNameToPath(packageName)
        try {
            return Files
                    .list(resourcesDirectoryFile.toPath().resolve(packagePath))
                    .filter { Files.isRegularFile(it) }
                    .filter { path -> STRINGS_FILE_PATTERN.matcher(path.fileName.toString()).matches() }
                    .map { it.toFile() }
                    .toList()
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }

    private fun loadProperties(path: Path): Properties {
        val properties = Properties()
        try {
            Files.newBufferedReader(path, extension.charset).use { reader -> properties.load(reader) }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
        return properties
    }

    private fun getFileFromObject(fileObject: Any?): File {
        requireNonNull<Any>(fileObject)
        return if (fileObject is Path) {
            fileObject.toFile()
        } else fileObject as? File ?: File(fileObject!!.toString())
    }

    private fun getOutputDirectory(packageName: String): File =
            File(getFileFromObject(extension.outputDirectory), packageNameToPath(packageName))

    private fun packageNameToPath(packageName: String): String = packageName.replace('.', File.separatorChar)

}
