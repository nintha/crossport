package none.nintha.crossport.client

import none.nintha.crossport.common.ByteArrayCodecFactory
import none.nintha.crossport.common.MinaConsts
import none.nintha.crossport.entity.InnerPack
import org.apache.mina.core.RuntimeIoException
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.transport.socket.nio.NioSocketConnector
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

class RemoteHandler(private val localPort: Int, val remotePortCallback: (Int) -> Unit) : IoHandlerAdapter() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val localSessionMap: MutableMap<String, IoSession> = ConcurrentHashMap()
    private var leftBytes = byteArrayOf()
    var outsidePort: Int = 0

    override fun messageReceived(session: IoSession, message: Any?) {
        val buf = message as ByteArray
        val packs = InnerPack.parse(leftBytes + buf)
        leftBytes = byteArrayOf()
        packs.forEach { pack ->
            if (pack.integrity == InnerPack.INTEGRITY_INCOMPLETE) {
                leftBytes = pack.leftBytes
                return
            }
            when (pack.type) {
                InnerPack.TYPE_ANSWER -> {
                    this.outsidePort = InnerPack.bytesToInt(pack.payload)
                    remotePortCallback(outsidePort)
                    logger.info("outside port is $outsidePort")
                }
                InnerPack.TYPE_FORWARD -> {
//                    logger.info("forward from ${pack.address}, pack length=${pack.totalLength}")
                    if (localSessionMap[pack.address] == null) {
                        val localSession = connectLocalApp(session, pack.address)
                        localSessionMap[pack.address] = localSession
                    }
                    localSessionMap[pack.address]!!.write(pack.payload)
                }
                InnerPack.TYPE_CLOSE -> {
                    logger.info("To close ${pack.address}")
                    localSessionMap.remove(pack.address)?.closeNow()
                }
            }
        }

    }

    private fun connectLocalApp(remoteSession: IoSession, sourceAddress: String): IoSession {
        val connector = NioSocketConnector()
        connector.connectTimeoutMillis = 100
        connector.filterChain.addLast("codec", ProtocolCodecFilter(ByteArrayCodecFactory()))
        connector.handler = LocalHandler(remoteSession, sourceAddress)
        var session: IoSession
        while (true) {
            try {
                val future = connector.connect(InetSocketAddress(MinaConsts.LOCALHOST, localPort))
                future.awaitUninterruptibly();
                session = future.session
                break;
            } catch (e: RuntimeIoException) {
                logger.error("Failed to connect LocalApp(${MinaConsts.LOCALHOST}:$localPort)", e)
                Thread.sleep(5000)
            }
        }
        logger.info("$sourceAddress connect to local port $localPort")
        return session
    }

    override fun sessionCreated(session: IoSession?) {
        logger.info("Remote client created, local port=$localPort")
    }

    override fun sessionClosed(session: IoSession?) {
        logger.info("Remote client closed, local port=$localPort")
    }

}