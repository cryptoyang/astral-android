package cc.cryptopunks.ui.service.stub

import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.service.base.Rpc

fun parseRpcCommand(
    method: String,
    args: Map<String, Any>
): Rpc.Command {
    val clazz = Class.forName(method) as Class<Rpc.Command>
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
