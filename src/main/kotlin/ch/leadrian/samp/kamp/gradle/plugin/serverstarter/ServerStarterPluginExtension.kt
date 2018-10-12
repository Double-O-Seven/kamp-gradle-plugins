package ch.leadrian.samp.kamp.gradle.plugin.serverstarter

import org.apache.commons.lang3.RandomStringUtils
import org.gradle.internal.os.OperatingSystem
import java.net.URI

open class ServerStarterPluginExtension {

    var gameModeClassName: String? = null

    val configProperties: MutableMap<String, Any> = mutableMapOf()

    var windowsServerDownloadUrl: String = "http://files.sa-mp.com/samp037_svr_R2-1-1_win32.zip"

    var linuxServerDownloadUrl: String = "http://files.sa-mp.com/samp037svr_R2-1.tar.gz"

    val downloadUrl: String
        get() = when {
            OperatingSystem.current().isWindows -> windowsServerDownloadUrl
            else -> linuxServerDownloadUrl
        }

    val downloadFileName: String
        get() = URI(downloadUrl).toURL().file.substring(1)

    var kampPluginBinaryPath: String? = null

    val jvmOptions: MutableList<String> = mutableListOf()

    var lanMode: Boolean = false

    var rconPassword: String = RandomStringUtils.random(8, true, true)

    var maxPlayers: Int = 100

    var port: Int = 7777

    var hostName: String = "SA-MP 0.3.7 Server"

    var announce: Boolean = false

    var chatLogging: Boolean = false

    var webUrl: String = "www.sa-mp.com"

    var onFootRate: Int = 40

    var inCarRate: Int = 40

    var weaponRate: Int = 40

    var streamDistance: Float = 300f

    var streamRate: Int = 1000

    var maxNPCs: Int = 0

    var logTimeFormat: String = "[%H:%M:%S]"

    var language: String = "English"

}