package cc.cryptopunks.ui.poc.schema.rpc

import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface Rpc {

    abstract class Api {
        val methods by lazy { rpcMethods() }
    }

    interface Method

    abstract class Result<T> : Method {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        val result: T? = null
    }

    abstract class Single<T> : Result<T>()

    abstract class Stream<T> : Result<T>()
}

fun Any.rpcMethods(): List<KClass<*>> =
    this::class.nestedClasses.filter { type -> type.isSubclassOf(Rpc.Method::class) }
