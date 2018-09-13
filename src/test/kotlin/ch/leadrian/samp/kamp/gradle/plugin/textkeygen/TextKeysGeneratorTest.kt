package ch.leadrian.samp.kamp.gradle.plugin.textkeygen

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.StringWriter
import java.util.stream.Stream

internal class TextKeysGeneratorTest {

    @ParameterizedTest
    @ArgumentsSource(GenerateTextKeyClassesArgumentsProvider::class)
    fun shouldGenerateTextKeyClasses(
            rootClassName: String,
            packageName: String,
            stringPropertyNames: Set<String>,
            expectedClassString: String
    ) {
        StringWriter().use { writer ->
            val textKeysGenerator = TextKeysGenerator()

            textKeysGenerator.generateTextKeyClasses(
                    rootClassName = rootClassName,
                    packageName = packageName,
                    stringPropertyNames = stringPropertyNames,
                    writer = writer
            )

            assertThat(writer.toString())
                    .isEqualTo(expectedClassString)
        }
    }

    private class GenerateTextKeyClassesArgumentsProvider : ArgumentsProvider {

        override fun provideArguments(context: ExtensionContext?): Stream<GenerateTextKeyClassesArguments> = Stream.of(
                GenerateTextKeyClassesArguments(
                        rootClassName = "TextKeys",
                        packageName = "ch.leadrian.samp.kamp.core.api.text",
                        stringPropertyNames = setOf(),
                        expectedClassString = ("package ch.leadrian.samp.kamp.core.api.text;\n\n" +
                                "import ch.leadrian.samp.kamp.core.api.text.TextKey;\n" +
                                "import javax.annotation.Generated;\n\n" +
                                "@Generated(\n\tvalue = \"ch.leadrian.samp.kamp.gradle.plugin.textkeygen.TextKeysGenerator\"\n)\n" +
                                "public final class TextKeys {\n\n" +
                                "\tprivate TextKeys() {}\n\n" +
                                "}\n\n")
                ),
                GenerateTextKeyClassesArguments(
                        rootClassName = "TextKeys",
                        packageName = "ch.leadrian.samp.kamp.core.api.text",
                        stringPropertyNames = setOf("test"),
                        expectedClassString = ("package ch.leadrian.samp.kamp.core.api.text;\n\n" +
                                "import ch.leadrian.samp.kamp.core.api.text.TextKey;\n" +
                                "import javax.annotation.Generated;\n\n" +
                                "@Generated(\n\tvalue = \"ch.leadrian.samp.kamp.gradle.plugin.textkeygen.TextKeysGenerator\"\n)\n" +
                                "public final class TextKeys {\n\n" +
                                "\tprivate TextKeys() {}\n\n" +
                                "\tpublic static final String test_ = \"test\";\n" +
                                "\tpublic static final TextKey test = new TextKey(test_);\n\n" +
                                "}\n\n")
                ),
                GenerateTextKeyClassesArguments(
                        rootClassName = "TextKeys",
                        packageName = "ch.leadrian.samp.kamp.core.api.text",
                        stringPropertyNames = setOf("test2", "test1"),
                        expectedClassString = ("package ch.leadrian.samp.kamp.core.api.text;\n\n" +
                                "import ch.leadrian.samp.kamp.core.api.text.TextKey;\n" +
                                "import javax.annotation.Generated;\n\n" +
                                "@Generated(\n\tvalue = \"ch.leadrian.samp.kamp.gradle.plugin.textkeygen.TextKeysGenerator\"\n)\n" +
                                "public final class TextKeys {\n\n" +
                                "\tprivate TextKeys() {}\n\n" +
                                "\tpublic static final String test1_ = \"test1\";\n" +
                                "\tpublic static final TextKey test1 = new TextKey(test1_);\n\n" +
                                "\tpublic static final String test2_ = \"test2\";\n" +
                                "\tpublic static final TextKey test2 = new TextKey(test2_);\n\n" +
                                "}\n\n")
                ),
                GenerateTextKeyClassesArguments(
                        rootClassName = "TextKeys",
                        packageName = "ch.leadrian.samp.kamp.core.api.text",
                        stringPropertyNames = setOf(
                                "test2.abc",
                                "test2.xyz.t1",
                                "test2.xyz.t2",
                                "test1",
                                "test3.lol"
                        ),
                        expectedClassString = ("package ch.leadrian.samp.kamp.core.api.text;\n\n" +
                                "import ch.leadrian.samp.kamp.core.api.text.TextKey;\n" +
                                "import javax.annotation.Generated;\n\n" +
                                "@Generated(\n\tvalue = \"ch.leadrian.samp.kamp.gradle.plugin.textkeygen.TextKeysGenerator\"\n)\n" +
                                "public final class TextKeys {\n\n" +
                                "\tprivate TextKeys() {}\n\n" +
                                "\tpublic static final String test1_ = \"test1\";\n" +
                                "\tpublic static final TextKey test1 = new TextKey(test1_);\n\n" +
                                "\tpublic static final class test2 {\n\n" +
                                "\t\tprivate test2() {}\n\n" +
                                "\t\tpublic static final String abc_ = \"test2.abc\";\n" +
                                "\t\tpublic static final TextKey abc = new TextKey(abc_);\n\n" +
                                "\t\tpublic static final class xyz {\n\n" +
                                "\t\t\tprivate xyz() {}\n\n" +
                                "\t\t\tpublic static final String t1_ = \"test2.xyz.t1\";\n" +
                                "\t\t\tpublic static final TextKey t1 = new TextKey(t1_);\n\n" +
                                "\t\t\tpublic static final String t2_ = \"test2.xyz.t2\";\n" +
                                "\t\t\tpublic static final TextKey t2 = new TextKey(t2_);\n\n" +
                                "\t\t}\n\n" +
                                "\t}\n\n" +
                                "\tpublic static final class test3 {\n\n" +
                                "\t\tprivate test3() {}\n\n" +
                                "\t\tpublic static final String lol_ = \"test3.lol\";\n" +
                                "\t\tpublic static final TextKey lol = new TextKey(lol_);\n\n" +
                                "\t}\n\n" +
                                "}\n\n")
                )
        )

    }

    private class GenerateTextKeyClassesArguments(
            private val rootClassName: String,
            private val packageName: String,
            private val stringPropertyNames: Set<String>,
            private val expectedClassString: String
    ) : Arguments {

        override fun get(): Array<Any> = arrayOf(rootClassName, packageName, stringPropertyNames, expectedClassString)

    }
}