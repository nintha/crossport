package none.nintha.crossport.entity

import java.io.ByteArrayInputStream
import java.io.Serializable
import java.util.*

class InnerPack : Serializable {
    companion object {
        const val HEADER_LENGTH_DEFAULT = 15
        const val TYPE_FORWARD = 0
        const val TYPE_CONNECT = 1
        const val TYPE_ANSWER = 2
        const val TYPE_CLOSE = 3
        const val INTEGRITY_NORMAL = 0
        const val INTEGRITY_OVERFLOW = 1
        const val INTEGRITY_INCOMPLETE = 2
        fun intToBytes(value: Int): ByteArray {
            val bytes = ByteArray(4)
            bytes[0] = value.shr(24).toByte()
            bytes[1] = value.shr(16).toByte()
            bytes[2] = value.shr(8).toByte()
            bytes[3] = value.toByte()
            return bytes
        }

        fun bytesToInt(bytes: ByteArray): Int = bytes.toList().map { it.toInt() and 0xFF }.reduce { a, b -> a * 256 + b }

        fun parse(bytes: ByteArray): ArrayList<InnerPack> {
            val packs: ArrayList<InnerPack> = arrayListOf()
            var leftBytes = bytes
            do {
                val pack = InnerPack(leftBytes)
                packs.add(pack)
                leftBytes = pack.leftBytes
            } while (pack.integrity == InnerPack.INTEGRITY_OVERFLOW)
            return packs
        }
    }

    // 总长度4byte
    var totalLength: Int = 0
    // 头部长度2byte
    var headerLength: Int = HEADER_LENGTH_DEFAULT
    // 版本2byte，默认为0
    var version: Int = 0
    // 来源地址6byte=4byte+2byte，eg:127.0.0.1:1234
    var address: String = ""
    // 来源地址中的IP
    var sourceIp: String = ""
    // 来源地址中的端口
    var sourcePort: Int = 0
    // 数据类型1byte 0-转发数据，1-请求：创建outside服务器，2-响应：outside服务器端口
    var type: Int = TYPE_FORWARD
    // 其余头部字段，未定义
    var restHeader: ByteArray = byteArrayOf()
    // 负载
    var payload: ByteArray = byteArrayOf()

    // 包完整度 0-完整 1-溢出 2-残缺
    var integrity: Int = INTEGRITY_NORMAL
    // 下一帧的数据, integrity=2情况下，leftBytes是本帧的数据
    var leftBytes: ByteArray = byteArrayOf()

    constructor(address: String, frameType: Int, payload: ByteArray) {
        val parts = address.split(":")
        sourceIp = parts[0]
        sourcePort = parts[1].toInt()
        totalLength = headerLength + payload.size
        this.type = frameType
        this.payload = payload
    }

    constructor(originBytes: ByteArray) {
        // 片段过短不包含完整头部信息
        if (originBytes.size < 4) {
            this.integrity = INTEGRITY_INCOMPLETE
            this.leftBytes = originBytes.copyOf()
            return
        }

        val stream = ByteArrayInputStream(originBytes)
        val readAsByteArray = { n: Int -> (1..n).map { stream.read().toByte() }.toByteArray() }
        val readAsInt = { n: Int -> bytesToInt(readAsByteArray(n)) }

        this.totalLength = readAsInt(4)
        when {
            originBytes.size > totalLength -> this.integrity = INTEGRITY_OVERFLOW
            originBytes.size < totalLength -> {
                this.integrity = INTEGRITY_INCOMPLETE
                this.leftBytes = originBytes
                return
            }
        }
        this.headerLength = readAsInt(2)
        this.version = readAsInt(2)
        this.sourceIp = readAsByteArray(4).map { it.toInt() and 0xFF }.joinToString(".")
        this.sourcePort = readAsInt(2)
        this.address = "$sourceIp:$sourcePort"
        this.type = readAsInt(1)
        this.restHeader = readAsByteArray(headerLength - HEADER_LENGTH_DEFAULT)
        this.payload = readAsByteArray(totalLength - headerLength)
        this.leftBytes = readAsByteArray(originBytes.size - totalLength)
    }

    /**
     * 把整个对象转换为Byte数组
     */
    fun toBytes(): ByteArray {
        val list: MutableList<Byte> = mutableListOf()
        list.addAll(intToBytes(totalLength).toList())
        list.addAll(intToBytes(headerLength).toList().subList(2, 4))
        list.addAll(intToBytes(version).toList().subList(2, 4))
        list.addAll(sourceIp.split(".").map { it.toInt().toByte() })
        list.addAll(intToBytes(sourcePort).toList().subList(2, 4))
        list.add(type.toByte())
        list.addAll(payload.toList())
        return list.toByteArray()
    }

    override fun toString(): String {
        return "InnerPack(totalLength=$totalLength, headerLength=$headerLength, version=$version, address='$address', sourceIp='$sourceIp', sourcePort=$sourcePort, type=$type, restHeader=${Arrays.toString(restHeader)}, payload=${Arrays.toString(payload)}, integrity=$integrity, leftBytes=${Arrays.toString(leftBytes)})"
    }


}

fun main(args: Array<String>) {
//    val fr = InnerPack("1.2.3.4:5678", InnerPack.TYPE_FORWARD, byteArrayOf(127, 101))
//    val bytes = fr.toBytes()
//    println(bytes.size)
//    println(bytes.toList())
//
//    val fr2 = InnerPack.parse(byteArrayOf(0, 0, 0, -1, 0, 15, 0, 0, 1, 2, 3, 4, 22, 46))
//    println(fr2.size)
//    fr2.forEach { println(it) }
    println("181".toInt().toByte().toInt() and 0xFF)
}