package ch.leadrian.samp.kamp.gradle.plugin.textkeygen

import java.io.Writer
import javax.annotation.Generated

class TextKeysGenerator {

    @Generated
    fun generateTextKeyClasses(rootClassName: String, packageName: String, stringPropertyNames: Set<String>, writer: Writer) {
        writer.write("package $packageName;\n\n")
        writer.write("import ch.leadrian.samp.kamp.core.api.text.TextKey;\n")
        writer.write("import javax.annotation.Generated;\n\n")
        writer.write("@Generated(\n\tvalue = \"${this::class.java.name}\"\n)\n")
        val root = getTextKeyTree(rootClassName, stringPropertyNames.map { TextKey(it) })
        root.isRoot = true
        root.write("", writer)
    }

    private fun getTextKeyTree(parentSegment: String, textKeys: List<TextKey>): TextKeyTree {
        val textKeysByFirstSegment = textKeys.groupBy(
                keySelector = { it.propertyNameSegments.first() },
                valueTransform = { it.copy(propertyNameSegments = it.propertyNameSegments.drop(1)) }
        )
        val subtreesBySegment = mutableListOf<TextKeyTree>()
        textKeysByFirstSegment.toSortedMap().forEach { segment, groupedTextKeys ->
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

    private sealed class TextKeyTree(val segment: String, var isRoot: Boolean = false) {

        abstract fun write(indentation: String, writer: Writer)

        class Node(segment: String, val subtrees: List<TextKeyTree>) : TextKeyTree(segment) {

            override fun write(indentation: String, writer: Writer) {
                if (isRoot) {
                    writer.write("${indentation}public final class $segment {\n\n")
                } else {
                    writer.write("${indentation}public static final class $segment {\n\n")
                }
                writer.write("$indentation\tprivate $segment() {}\n\n")
                subtrees.forEach {
                    it.write("$indentation\t", writer)
                }
                writer.write("$indentation}\n\n")
            }
        }

        class Leaf(segment: String, val propertyName: String) : TextKeyTree(segment) {

            override fun write(indentation: String, writer: Writer) {
                writer.write("${indentation}public static final String ${segment}_ = \"$propertyName\";\n")
                writer.write("${indentation}public static final TextKey $segment = new TextKey(${segment}_);\n\n")
            }
        }
    }

    private data class TextKey(val propertyName: String, val propertyNameSegments: List<String>) {
        constructor(propertyName: String) : this(propertyName, propertyName.split(".").toList())
    }

}
