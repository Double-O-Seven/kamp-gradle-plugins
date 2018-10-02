package ch.leadrian.samp.kamp.gradle.plugin.textkeygen

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

open class TextKeysGeneratorPluginExtension {

    var outputDirectory: Any? = null
    var resourcesDirectory: Any? = null
    var packageNames: Set<String> = HashSet()
    var charset: Charset = StandardCharsets.ISO_8859_1
}
