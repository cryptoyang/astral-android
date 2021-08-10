package cc.cryptopunks.astral.stream

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

class AstralError(error: String) : Exception(error)
