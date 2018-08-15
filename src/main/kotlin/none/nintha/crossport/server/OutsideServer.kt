package none.nintha.crossport.server

import none.nintha.crossport.common.ByteArrayCodecFactory
import none.nintha.crossport.common.NetUtils
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.transport.socket.SocketAcceptor
import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class OutsideServer(insideSession: IoSession) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    val bindPort = NetUtils.findFreePort()
    val handler = OutsideHandler(insideSession,bindPort)

    init{
        val acceptor: SocketAcceptor = NioSocketAcceptor()
        val chain = acceptor.filterChain
        val filter = ProtocolCodecFilter(ByteArrayCodecFactory())
        chain.addLast("objectFilter", filter)

        acceptor.handler = handler
        acceptor.bind(InetSocketAddress(bindPort))
        logger.info("OutsideServer listen in $bindPort")
    }
}