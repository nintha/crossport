package none.nintha.crossport.server

import none.nintha.crossport.common.MinaConsts
import none.nintha.crossport.common.NetUtils
import none.nintha.crossport.entity.InnerPack
import org.apache.mina.core.buffer.IoBuffer
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IoSession
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

class InsideHandler : IoHandlerAdapter() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    // remoteClientAddress => { sourceAddress => session}
    private val clientMap: MutableMap<String, MutableMap<String, IoSession>> = ConcurrentHashMap<String, MutableMap<String, IoSession>>()
    //    val portMap : MutableMap<Int, Int> = ConcurrentHashMap() // sourcePort => outsidePort
    private val leftBytesMap: MutableMap<String, ByteArray> = ConcurrentHashMap() // clientAddress => leftBytes

    override fun messageReceived(session: IoSession, message: Any) {
        val clientAddress = session.getAttribute(MinaConsts.ATTR_SESSION_ADDRESS) as String
        val buf = message as ByteArray
        val packs = InnerPack.parse(leftBytesMap.getOrDefault(clientAddress, byteArrayOf()) + buf)
        leftBytesMap.remove(clientAddress)

        packs.forEach { pack ->
            if (pack.integrity == InnerPack.INTEGRITY_INCOMPLETE) {
                leftBytesMap[clientAddress] = pack.leftBytes
                return@forEach
            }
            when (pack.type) {
                InnerPack.TYPE_CONNECT -> {
                    val bindPort = session.getAttribute(MinaConsts.ATTR_OUTSIDE_PORT) as Int
                    logger.info("Answer $clientAddress, outside port=$bindPort")

                    val bytes = InnerPack(clientAddress, InnerPack.TYPE_ANSWER, InnerPack.intToBytes(bindPort)).toBytes()
                    session.write(IoBuffer.wrap(bytes))
                }
                InnerPack.TYPE_FORWARD -> {
                    try {
//                        logger.info("forward to ${pack.address}, pack length=${pack.totalLength}, outsideSessionMap=${clientMap[clientAddress]}")
                        clientMap[clientAddress]!![pack.address]?.write(pack.payload)
                    } catch (e: Exception) {
                        logger.error("pack.address=${pack.address}, clientAddress=$clientAddress", e)
                    }
                }
                InnerPack.TYPE_CLOSE -> {
                    try {
                        logger.info("To close ${pack.address}")
                        clientMap[clientAddress]!![pack.address]?.closeNow()
                    } catch (e: Exception) {
                        logger.error("pack.address=${pack.address}, clientAddress=$clientAddress", e)
                    }
                }
            }
        }
    }


    override fun sessionCreated(session: IoSession) {
        val address = NetUtils.getIPv4Port(session.remoteAddress as InetSocketAddress)
        session.setAttribute(MinaConsts.ATTR_SESSION_ADDRESS, address)

        val outsideServer = OutsideServer(session)
        clientMap[address] = outsideServer.handler.sessionMap

        logger.info("inside connection from $address, outside port=${outsideServer.bindPort}")
        session.setAttribute(MinaConsts.ATTR_OUTSIDE_PORT, outsideServer.bindPort)

        val bytes = InnerPack(address, InnerPack.TYPE_ANSWER, InnerPack.intToBytes(outsideServer.bindPort)).toBytes()
        session.write(bytes)
    }

    override fun sessionClosed(session: IoSession?) {
        val address = session!!.getAttribute(MinaConsts.ATTR_SESSION_ADDRESS) as String
        clientMap.remove(address)!!.forEach { _, v -> v.closeNow() }
        logger.info("$address inside session closed")
    }

    override fun exceptionCaught(session: IoSession?, cause: Throwable?) {
        val address = session!!.getAttribute(MinaConsts.ATTR_SESSION_ADDRESS) as String
        logger.error("$address inside session error", cause)
    }

}