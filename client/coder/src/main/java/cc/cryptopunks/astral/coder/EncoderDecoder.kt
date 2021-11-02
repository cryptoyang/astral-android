package cc.cryptopunks.astral.coder

fun interface Encoder : (Any) -> String

interface Decoder {
    operator fun <T> invoke(bytes: String, type: Class<T>): T
}

inline operator fun <reified T> Decoder.invoke(bytes: String): T =
    invoke(bytes, T::class.java)
