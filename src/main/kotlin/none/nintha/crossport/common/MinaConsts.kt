package none.nintha.crossport.common

class MinaConsts {
    companion object {
        const val OUTSIDE_PORT_START = 40000
        const val OUTSIDE_PORT_END = 49999

        const val ATTR_SESSION_ADDRESS = "ATTR_SESSION_ADDRESS"
        const val ATTR_OUTSIDE_PORT = "ATTR_OUTSIDE_PORT"

        const val LOCALHOST = "127.0.0.1"
        const val INSIDE_PORT_DEFAULT = 5566
        const val INSIDE_IP_DEFAULT = "127.0.0.1"

        var insidePort = INSIDE_PORT_DEFAULT
        var insideIp = INSIDE_IP_DEFAULT
    }
}
