package none.nintha.crossport.server

import none.nintha.crossport.common.ByteArrayCodecFactory
import none.nintha.crossport.common.MinaConsts
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.transport.socket.SocketAcceptor
import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class InsideServer {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val bindPort = MinaConsts.insidePort
    fun init() {
        val acceptor: SocketAcceptor = NioSocketAcceptor()
        val chain = acceptor.filterChain
        val filter = ProtocolCodecFilter(ByteArrayCodecFactory())
        chain.addLast("objectFilter", filter)

        acceptor.handler = InsideHandler()

        acceptor.bind(InetSocketAddress(bindPort))
        logger.info("insideServer listen in $bindPort")
    }

}

fun main(args: Array<String>) {
    InsideServer().init()
}