package none.nintha.crossport.common

import org.apache.mina.core.buffer.IoBuffer
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.*

class ByteArrayCodecFactory : ProtocolCodecFactory {
    private val encoder = ByteArrayEncoder()
    private val decoder = ByteArrayDecoder()

    override fun getEncoder(session: IoSession?): ProtocolEncoder {
        return encoder
    }

    override fun getDecoder(session: IoSession?): ProtocolDecoder {
        return decoder
    }


    class ByteArrayEncoder : ProtocolEncoderAdapter() {
        override fun encode(session: IoSession, message: Any, out: ProtocolEncoderOutput) {
            val bytes = message as ByteArray
            val buffer = IoBuffer.allocate(256)
            buffer.isAutoExpand = true
            buffer.put(bytes)
            buffer.flip()

            out.write(buffer)
            out.flush()

            buffer.free()
        }
    }

    class ByteArrayDecoder : ProtocolDecoderAdapter() {
        override fun decode(session: IoSession, `in`: IoBuffer, out: ProtocolDecoderOutput) {
            val limit = `in`.limit()
            val bytes = ByteArray(limit)

            `in`.get(bytes)

            out.write(bytes)
        }

    }
}