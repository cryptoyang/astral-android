package cc.cryptopunks.ui.poc.stub

import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.model.Service
import cc.cryptopunks.ui.poc.model.UIRequestData
import cc.cryptopunks.ui.poc.transport.schema.rpc.Rpc
import kotlinx.coroutines.flow.map

val uiRequestData: UIRequestData = {
    val command = parseRpcMethod(context.schema, method, args)
    handle(command).map(Jackson.jsonMapper::valueToTree)
}

fun parseRpcMethod(
    schema: Service.Schema,
    method: Service.Method,
    args: Map<String, Any>
): Rpc.Command {
    val fullName = schema.id + "$" + method.id
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
