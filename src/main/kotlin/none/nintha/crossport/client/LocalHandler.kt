package none.nintha.crossport.client

import none.nintha.crossport.entity.InnerPack
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IoSession
import org.slf4j.LoggerFactory

class LocalHandler(private val remoteSession: IoSession, private val sourceAddress: String) : IoHandlerAdapter() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun messageReceived(session: IoSession, message: Any?) {
        val buf = message as ByteArray
        val pack = InnerPack(sourceAddress, InnerPack.TYPE_FORWARD, buf)
        remoteSession.write(pack.toBytes())
//        logger.info("forward to $sourceAddress, pack length=${pack.totalLength}")
    }

    override fun sessionClosed(session: IoSession?) {
        val pack = InnerPack(sourceAddress, InnerPack.TYPE_CLOSE, byteArrayOf())
        remoteSession.write(pack.toBytes())
        logger.info("$sourceAddress local session closed")
    }
}