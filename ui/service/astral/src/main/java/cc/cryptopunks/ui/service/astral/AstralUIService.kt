package cc.cryptopunks.ui.service.astral

import cc.cryptopunks.astral.api.*
import cc.cryptopunks.binary.bytes
import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.service.schema.OpenRpc
import cc.cryptopunks.ui.service.schema.toSchema
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AstralUIService(
    private val network: Network
) :
    Service.Interface,
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    private val port = "ui"
    private val nodeId = "024d47047667312be7cd0a14033323b716030f5fc9dd7ae774eb96527a76fa55f9"

    private object Method {
        val Schemas = "schema"
        val Request = "request"
        val Subscribe = "subscribe"
    }

    override fun execute(request: Service.Request): Job = launch {
        val command = formatRpcCommand(request.method, request.args)
        network.run {
            connect(nodeId, connect(nodeId, port).run {
                writeL8String(Method.Request)
                readL8String()
            }).run {
                val (destPort, method) = request.method.split("/")
                writeL8String(destPort)
                write(
                    Jackson.jsonMapper.writeValueAsBytes(
                        RpcJsonRequest(
                            method = method,
                            params = request.args.values.toList()
                        )
                    )
                )
            }
        }
    }

    override fun subscribe(request: Service.Request): Flow<Any> = channelFlow {
        val command = formatRpcCommand(request.method, request.args)
        withContext(Dispatchers.IO) {
            network.run {
                val queryPort = connect(nodeId, port).run {
                    writeL8String(Method.Subscribe)
                    readL8String()
                }
                connect(nodeId, queryPort).run {
                    val (destPort, method) = request.method.split("/")
                    writeL8String(destPort)
                    write(
                        Jackson.jsonMapper.writeValueAsBytes(
                            RpcJsonRequest(
                                method = method,
                                params = request.args.values.toList()
                            )
                        )
                    )
                    println("reading subscribed elements")
                    inputStream().reader().forEachLine { line ->
                        val data = Jackson.jsonMapper.readValue<Map<String, Any>>(line).getValue("result")
                        trySendBlocking(data)
                    }
                }
            }
        }
    }

    override fun schemas(
        known: Set<Service.Schema.Version>
    ): Flow<Service.Schema> = channelFlow {
        val arg = Jackson.jsonSlimMapper.writeValueAsString(known)
        withContext(Dispatchers.IO) {
            network.run {
                connect(nodeId,
                    connect(nodeId, port).run {
                        writeL8String(Method.Schemas)
                        readL8String()
                    }
                ).run {
//                    writeString(arg) { arg.length.bytes }
                    while (true) {
                        val bytes = readL64Bytes()
                        val doc = Jackson.jsonMapper.readValue<OpenRpc.Document>(bytes)
                        val schema = doc.toSchema()
                        send(schema)
                    }
                }
            }
        }
    }

    override fun status(): Flow<Service.Status> {
        TODO("Not yet implemented")
    }
}

private fun formatRpcCommand(
    method: String,
    args: Map<String, Any>
): String =
    method + ":" + Jackson.jsonSlimMapper.writeValueAsString(args)

data class RpcJsonRequest(
    val method: String,
    val params: List<Any>,
    val id: Long = 0,
)
