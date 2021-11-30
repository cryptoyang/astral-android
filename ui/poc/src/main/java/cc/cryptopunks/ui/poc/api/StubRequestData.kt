package cc.cryptopunks.ui.poc.api

import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.UIRequestData
import cc.cryptopunks.ui.poc.schema.rpc.Rpc
import kotlinx.coroutines.flow.map

val uiRequestData: UIRequestData = {
    val command = parseRpcMethod(context.model, method, args)
    handle(command).map(Jackson.jsonMapper::valueToTree)
}

fun parseRpcMethod(
    model: Api.Model,
    method: Api.Method,
    args: Map<String, Any>
): Rpc.Command {
    val fullName = model.id + "$" + method.id
    val clazz = Class.forName(fullName) as Class<Rpc.Command>
    val rpcCommand: Rpc.Command = when {
        args.isEmpty() ->
            Jackson.jsonMapper.readValue(EmptyJson, clazz)
        else -> {
            val json = Jackson.jsonPrettyWriter.writeValueAsString(args)
            println(json)
            Jackson.jsonMapper.readValue(json, clazz)
        }
    }
    return rpcCommand
}

const val EmptyJson = "{}"
