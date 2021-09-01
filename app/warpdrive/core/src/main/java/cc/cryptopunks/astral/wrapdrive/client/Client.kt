package cc.cryptopunks.astral.wrapdrive.client

import android.util.Log
import binary.Binary
import cc.cryptopunks.astral.api.Network
import cc.cryptopunks.astral.api.inputStream
import cc.cryptopunks.astral.api.outputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

const val List: Byte = 1
const val SendStream: Byte = 3
const val Port = "wdrive-local"
private const val R_OK: Byte = 0

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun Network.listPeers(): List<String> = withContext(Dispatchers.IO) {
    connect("", Port).run {
        write(Binary.int8UBytes(List))
        inputStream().run {
            (0 until read()).map {
                String(readN(read()))
            }
        }.also { close() }
    }
}


@Suppress("BlockingMethodInNonBlockingContext")
suspend fun Network.sendFile(nodeId: String, fileName: String, inputStream: InputStream) =
    withContext(Dispatchers.IO) {
        connect("", Port).run {
            write(Binary.int8UBytes(SendStream))
            nodeId.toByteArray().run {
                write(Binary.int8UBytes(size.toByte()))
                write(this)
            }
            fileName.toByteArray().run {
                write(Binary.int16UBytes(size.toShort()))
                write(this)
            }
            Log.d("Client", "reading response")
            if (inputStream().readN(1).first() == R_OK) {
                Log.d("Client", "sending stream")
                outputStream().also(inputStream::copyTo)
                Log.d("Client", "sending finish")
            } else {
                Log.d("Client", "sending rejected")
            }
            close()
        }
    }

fun InputStream.readN(n: Int) = ByteArray(n)
    .also { buff -> check(read(buff) == n) }


