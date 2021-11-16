package cc.cryptopunks.ui.poc.api

import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.UIRequestData
import cc.cryptopunks.ui.poc.schema.rpc.Rpc

val uiRequestData: UIRequestData = {
    val command = parseRpcMethod(context.model, method, args) as MessengerApi.Method
    handle(command)
}

fun parseRpcMethod(
    model: Api.Model,
    method: Api.Method,
    args: Map<String, Any>
): Rpc.Method {
    val fullName = model.id + "$" + method.id
    val clazz = Class.forName(fullName) as Class<Rpc.Method>
    val rpcMethod: Rpc.Method = when {
        args.isEmpty() ->
            Jackson.jsonMapper.readValue(EmptyJson, clazz)
        else -> {
            val json = Jackson.prettyWriter.writeValueAsString(args)
            println(json)
            Jackson.jsonMapper.readValue(json, clazz)
        }
    }
    return rpcMethod
}

const val EmptyJson = "{}"
