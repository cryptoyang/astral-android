package cc.cryptopunks.astral.api

import android.util.Log
import com.google.gson.Gson
import java.io.Reader
import java.io.Writer
import java.lang.StringBuilder
import java.net.Socket

data class Request(
    val type: Type,
    val identity: String,
    val port: String,
    val path: String? = null,
) {
    enum class Type { connect, register }
}

data class Response(
    val status: String,
    val error: String = "",
) {
    enum class Status { ok, error }
}

data class ReadWriteSocket(
    val writer: Writer,
    val reader: Reader,
)

val gson = Gson()

fun Socket.readWriteSocket() = ReadWriteSocket(
    writer = getOutputStream().writer().buffered(4096),
    reader = getInputStream().reader().buffered(4096),
)

//fun ReadWriteSocket.readJsonRequest(): Request =
//    gson.fromJson(reader, Request::class.java)

//fun ReadWriteSocket.readJsonRequest(): Request {
//    val buffer = CharArray(8192)
//    reader.readText()
//    val len = reader.read(buffer)
//    val string = String(buffer.copyOf(len))
//    return gson.fromJson(string, Request::class.java)
//}

fun ReadWriteSocket.readJsonRequest(): Request {
    val string = reader.readFrame()
    return gson.fromJson(string, Request::class.java)
}

fun Reader.readFrame(): String {
    val result = StringBuilder()
//    val buffer = CharArray(1024)
    val buffer = CharArray(8)
    var len: Int
    do {
        len = read(buffer)
        result.append(buffer.copyOf(len))
    } while (len == buffer.size)
    return result.toString()
}

fun ReadWriteSocket.writeOk() =
    gson.run {
        toJson(Response("ok"), writer)
        writer.flush()
    }

fun ReadWriteSocket.writeJsonRequest(
    type: Request.Type,
    identity: String,
    port: String,
    path: String? = null,
) =
    gson.run {
        toJson(Request(type, identity, port, path), writer)
        writer.flush()

        val buff = CharArray(128)
        val len = reader.read(buff)
        val string = String(buff.copyOf(len))
        val response = gson.fromJson(string, Response::class.java)
        if (response.status != "ok")
            throw AstralError(response.error)
        else
            response.status
    }


class AstralError(error: String) : Exception(error)
