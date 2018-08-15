package none.nintha.crossport

import javafx.application.Application
import none.nintha.crossport.common.MinaConsts
import none.nintha.crossport.server.InsideServer

fun main(args: Array<String>) {
    var clientMode = true
    args.forEach {
        when {
            it == "--serverMode" -> clientMode = false
            it.startsWith("--listenPort=") -> {
                MinaConsts.insidePort = it.substringAfter("--listenPort=").toInt()
            }
            it.startsWith("--serverAddress=") -> {
                val address = it.substringAfter("--serverAddress=")
                with(address.split(":")) {
                    MinaConsts.insideIp = this[0]
                    MinaConsts.insidePort = this[1].toInt()
                }
            }
        }
    }

    if (clientMode) Application.launch(MainApp::class.java) else InsideServer().init()
}