package none.nintha.crossport.server

import none.nintha.crossport.common.MinaConsts
import none.nintha.crossport.common.NetUtils
import none.nintha.crossport.entity.InnerPack
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IoSession
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

class OutsideHandler(private val insideSession: IoSession, val outsidePort: Int) : IoHandlerAdapter() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    val sessionMap: MutableMap<String, IoSession> = ConcurrentHashMap()


    override fun messageReceived(session: IoSession?, message: Any?) {
        val buf = message as ByteArray
        val address = session!!.getAttribute(MinaConsts.ATTR_SESSION_ADDRESS) as String
        val pack = InnerPack(address, InnerPack.TYPE_FORWARD, buf)
        insideSession.write(pack.toBytes())

//        logger.info("forward from $address, pack length=${pack.totalLength}, ${insideSession.isActive}")
    }

    override fun sessionCreated(session: IoSession) {
        val address = NetUtils.getIPv4Port(session.remoteAddress as InetSocketAddress)
        session.setAttribute(MinaConsts.ATTR_SESSION_ADDRESS, address)
        sessionMap[address] = session
        logger.info("$address => $outsidePort connected, sessionMap=$sessionMap")
    }

    override fun sessionClosed(session: IoSession) {
        val address = session.getAttribute(MinaConsts.ATTR_SESSION_ADDRESS) as String
        sessionMap.remove(address)
        val pack = InnerPack(address, InnerPack.TYPE_CLOSE, byteArrayOf())
        insideSession.write(pack.toBytes())
        logger.info("$address outside session closed")
    }

    override fun exceptionCaught(session: IoSession?, cause: Throwable?) {
        val address = session!!.getAttribute(MinaConsts.ATTR_SESSION_ADDRESS) as String
        logger.info("$address outside session error, $cause")
    }

}