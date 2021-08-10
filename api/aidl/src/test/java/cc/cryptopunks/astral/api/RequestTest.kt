package cc.cryptopunks.astral.api

import org.junit.Test

class ProtoKtTest {

    @Test
    fun test() {
        gson.toJson(Request(
            type = Request.Type.register,
            identity = "identity",
            port = "port",
            path = "path",
        )).let {
            println(it)
        }

        gson.fromJson("""{"status":"ok"}""", Response::class.java).let {
            println(it)
        }
    }
}
