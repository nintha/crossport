package none.nintha.crossport.client

import none.nintha.crossport.common.ByteArrayCodecFactory
import none.nintha.crossport.common.MinaConsts
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.transport.socket.nio.NioSocketConnector
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class RemoteClient(val localPort: Int,
                   val serverIp: String = MinaConsts.insideIp,
                   val serverPort: Int = MinaConsts.insidePort) {
    private val logger = LoggerFactory.getLogger("RemoteClient")
    private lateinit var connector: NioSocketConnector
    private lateinit var session: IoSession
    fun connect(remotePortCallback: (RemoteClient) -> (Int) -> Unit = { _ -> {} }) {
        connector = NioSocketConnector()
        connector.connectTimeoutMillis = 3000
        connector.filterChain.addLast("codec", ProtocolCodecFilter(ByteArrayCodecFactory()))
        connector.handler = RemoteHandler(localPort, remotePortCallback(this))

        val future = connector.connect(InetSocketAddress(serverIp, serverPort))
        future.awaitUninterruptibly();
        session = future.session

        logger.info("RemoteClient connected to $serverIp:$serverPort")
    }

    fun close() {
        session.closeNow()
        connector.dispose()
    }
}
