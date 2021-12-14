package cc.cryptopunks.ui.poc.stub

import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.model.UIRequestData
import cc.cryptopunks.ui.poc.transport.schema.rpc.Rpc
import kotlinx.coroutines.flow.map

val uiRequestData: UIRequestData = {
    val command = parseRpcMethod(method.id, args)
    handle(command).map(Jackson.jsonMapper::valueToTree)
}

fun parseRpcMethod(
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
