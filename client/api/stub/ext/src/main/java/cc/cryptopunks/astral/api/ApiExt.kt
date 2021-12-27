package cc.cryptopunks.astral.api

import cc.cryptopunks.binary.byte
import cc.cryptopunks.binary.bytes
import cc.cryptopunks.binary.int
import cc.cryptopunks.binary.long
import cc.cryptopunks.binary.short
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

suspend fun Network.register(
    port: String,
    handle: suspend Stream.() -> Unit,
) = coroutineScope {
    val handler = register(port)
    while (true) {
        val connection = handler.next()
        println("next connection")
        launch(Dispatchers.IO) {
            val stream = connection.accept()
            println("accepted")
            try {
                stream.handle()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                stream.close()
                println("closed")
            }
        }
    }
}

suspend fun Network.connect(
    identity: String,
    port: String,
    handle: suspend Stream.() -> Unit,
) = withContext(Dispatchers.IO) {
    val stream = connect(identity, port)
    try {
        stream.handle()
    } catch (e: Throwable) {
        e.printStackTrace()
    } finally {
        stream.close()
    }
}

fun Stream.readMessage(): String? {
    val result = StringBuilder()
    val buffer = ByteArray(4096)
    var len: Int
    do {
        len = read(buffer)
        if (len > 0) result.append(String(buffer.copyOf(len)))
    } while (len == buffer.size)
    return when {
        len == -1 && result.isEmpty() -> null
        else -> result.toString()
    }
}

fun Stream.readMessage(handle: (String) -> Unit): Boolean =
    readMessage()?.let(handle) != null

fun Stream.inputStream(): InputStream = InputStreamWrapper(this::read)
fun Stream.outputStream(): OutputStream = OutputStreamWrapper(this::write)

fun InputStream.readN(n: Number) = ByteArray(n.toInt())
    .also { buff -> check(read(buff) == n) }

fun Stream.readN(n: Number) = ByteArray(n.toInt()).also { buff ->
    val len = read(buff)
    check(len == n.toInt()) { "Expected $n bytes but was $len" }
}

var Stream.byte: Byte
    get() = readN(1).byte
    set(value) {
        write(value.bytes)
    }

var Stream.short: Short
    get() = readN(2).short
    set(value) {
        write(value.bytes)
    }

var Stream.int: Int
    get() = readN(4).int
    set(value) {
        write(value.bytes)
    }

var Stream.long: Long
    get() = readN(8).long
    set(value) {
        write(value.bytes)
    }

fun Stream.readString(readSize: Stream.() -> Number) = String(readN(readSize()))

fun Stream.writeString(string: String, getSize: String.() -> ByteArray) {
    write(string.getSize())
    write(string.toByteArray())
}

fun Stream.writeL8String(string: String) {
    writeString(string) { string.length.toByte().bytes }
}

fun Stream.readL8String(): String =
    readString { byte }


fun Stream.readL64Bytes(): ByteArray =
    readN(long)
