package cc.cryptopunks.astral.api

import java.io.InputStream
import java.io.OutputStream


class InputStreamWrapper(
    bufferSize: Int = 4096,
    private val read: (ByteArray) -> Int,
) : InputStream() {
    private val buffer = ByteArray(bufferSize)
    private var offset = 0
    private var len = 0
    override fun read(): Int {
        if (len == -1) return -1
        if (offset == len) {
            len = read.invoke(buffer)
            offset = 0
        }
        return buffer[offset++].toInt()
    }
}

class OutputStreamWrapper(
    private val write: (ByteArray) -> Int,
) : OutputStream() {

    override fun write(b: Int) {
        val buff = ByteArray(1)
        buff[0] = b.toByte()
        write(buff)
    }
    override fun write(b: ByteArray) {
        write.invoke(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        write.invoke(b.copyOfRange(off, off + len))
    }
}
