package cc.cryptopunks.astral.coder

import com.google.gson.Gson

private val defaultGson by lazy {
    Gson().newBuilder()
        .setFieldNamingStrategy { it.name.replaceFirstChar(Char::uppercase) }
        .create()
}

class GsonEncoder(private val gson: Gson = defaultGson) : Encoder {
    override fun invoke(any: Any): String = gson.toJson(any)
}

class GsonDecoder(private val gson: Gson = defaultGson) : Decoder {
    override fun <T> invoke(bytes: String, type: Class<T>): T = gson.fromJson(bytes, type)
}

