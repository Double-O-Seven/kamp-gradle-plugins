package ch.leadrian.samp.kamp.textkeygen

import java.io.File
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Files.newBufferedWriter
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE

class TextKeysGenerator {

    fun generateTextKeyClasses(packageName: String, outputDirectory: Path, stringPropertyNames: Set<String>) {
        Files.createDirectories(outputDirectory.resolve(packageName.replace("\\.".toRegex(), File.separator)))
        newBufferedWriter(outputDirectory.resolve("TextKeys.java"), CREATE, WRITE).use { writer ->
            writer.write("package $packageName\n\n")
            writer.write("import ch.leadrian.samp.kamp.core.api.text.TextKey;\n\n")
            val root = getTextKeyTree("TextKeys", stringPropertyNames.map { TextKey(it) })
            root.write("", writer)
        }
    }

    private fun getTextKeyTree(parentSegment: String, textKeys: List<TextKey>): TextKeyTree {
        val textKeysByFirstSegment = textKeys.groupBy(
                keySelector = { it.propertyNameSegments.first() },
                valueTransform = { it.copy(propertyNameSegments = it.propertyNameSegments.drop(1)) }
        )
        val subtreesBySegment = mutableListOf<TextKeyTree>()
        textKeysByFirstSegment.forEach { segment, groupedTextKeys ->
            if (groupedTextKeys.size == 1) {
                groupedTextKeys.first().apply {
                    subtreesBySegment += when {
                        propertyNameSegments.isEmpty() -> TextKeyTree.Leaf(segment, propertyName)
                        else -> getTextKeyTree(segment, listOf(this))
                    }
                }
            } else {
                val leaf = groupedTextKeys.find { it.propertyNameSegments.isEmpty() }
                if (leaf != null) throw IllegalStateException("Property ${leaf.propertyName} cannot be a prefix of other properties")
                subtreesBySegment += getTextKeyTree(segment, groupedTextKeys)
            }
        }
        return TextKeyTree.Node(parentSegment, subtreesBySegment)
    }

    private sealed class TextKeyTree(val segment: String) {

        abstract fun write(indentation: String, writer: Writer)

        class Node(segment: String, val subtrees: List<TextKeyTree>) : TextKeyTree(segment) {

            override fun write(indentation: String, writer: Writer) {
                writer.write("${indentation}public final class $segment {\n\n")
                writer.write("$indentation\tprivate $segment() {}\n\n")
                subtrees.forEach {
                    it.write("$indentation\t", writer)
                }
                writer.write("$indentation}\n\n")
            }
        }

        class Leaf(segment: String, val propertyName: String) : TextKeyTree(segment) {

            override fun write(indentation: String, writer: Writer) {
                writer.write("${indentation}public static final String $segment = \"$propertyName\";\n")
                writer.write("${indentation}public static final TextKey ${segment}TextKey = new TextKey(\"$propertyName\");\n\n")
            }
        }
    }

    private data class TextKey(val propertyName: String, val propertyNameSegments: List<String>) {
        constructor(propertyName: String) : this(propertyName, propertyName.split("\\.").toList())
    }

}
