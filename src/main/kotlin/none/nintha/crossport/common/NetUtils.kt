package none.nintha.crossport.common

import java.io.IOException
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.net.ServerSocket

class NetUtils {
    companion object {
        private const val initPort = MinaConsts.OUTSIDE_PORT_START
        private var nextPort = initPort
        fun findFreePort(): Int {
            if (nextPort > MinaConsts.OUTSIDE_PORT_END) {
                nextPort = initPort
            }
            do {
                try {
                    val serverSocket = ServerSocket(nextPort)
                    serverSocket.close();
                    break;
                } catch (e: IOException) {
                    nextPort++;
                }
            } while (true)
            return nextPort++;
        }

        fun getIPv4Port(address: InetSocketAddress): String {
            val ip= when(address.address){
                is Inet6Address ->  "127.0.0.1"
                is Inet4Address ->  address.address.hostAddress
                else -> "0.0.0.0"
            }
            return "$ip:${address.port}"
        }

        fun checkIPv4Port(address: String): Boolean{
            try {
                if(address.contains(":")){
                    val parts = address.split(":")
                    if(parts.size != 2) return false

                    val regex =  """^((2[0-4]\d|25[0-5]|[01]?\d\d?)\.){3}(2[0-4]\d|25[0-5]|[01]?\d\d?)$""".toRegex()
                    return parts[1].toInt() in 1..65535 && regex.matches(parts[0])
                }
            }catch (e: Exception){
            }
            return false
        }
    }
}

fun main(args: Array<String>) {
//    val iPv4Port = NetUtils.getIPv4Port(InetSocketAddress("::1", 8888))
//    println(iPv4Port)
//    println(NetUtils.checkIPv4Port("127.0.0.1:65536"))
    NetUtils.findFreePort()
    NetUtils.findFreePort()
    println(NetUtils.findFreePort())
}