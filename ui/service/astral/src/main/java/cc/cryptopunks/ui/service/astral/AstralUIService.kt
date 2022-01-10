package cc.cryptopunks.ui.service.astral

import cc.cryptopunks.astral.api.*
import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.service.schema.OpenRpc
import cc.cryptopunks.ui.service.schema.toSchema
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class AstralUIService(
    private val network: Network
) :
    Service.Interface,
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    private val port = "ui"
    private val nodeId = ""

    private object Method {
        val Schemas = "schema"
        val Request = "request"
        val Subscribe = "subscribe"
    }

    override fun execute(request: Service.Request): Job = launch {
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
        val stream = withContext(Dispatchers.IO) {
            network.run {
                val queryPort = connect(nodeId, port).run {
                    writeL8String(Method.Subscribe)
                    readL8String()
                }
                connect(nodeId, queryPort)
            }
        }
        launch(Dispatchers.IO) {
            stream.run {
                val (destPort, method) = request.method.split("/")
                writeL8String(destPort)
                val rpcRequest = RpcJsonRequest(
                    method = method,
                    params = request.args.values.toList(),
                    id = 1
                )
                println("sending rpc request")
                println(Jackson.jsonPrettyWriter.writeValueAsString(rpcRequest))
                write(Jackson.jsonMapper.writeValueAsString(rpcRequest).toByteArray())
                println("reading subscribed elements")
                inputStream().reader().runCatching {
                    forEachLine { line ->
                        println(line)
                        val data = Jackson.jsonSlimMapper.readValue<Map<String, Any>>(line)
                        val result = data.getValue("result")
                        trySendBlocking(result)
                    }
                }

                println("finish reading responses of $request")
            }
        }
        awaitClose {
            stream.close()
        }
    }

    override fun schemas(
        known: Set<Service.Schema.Version>
    ): Flow<Service.Schema> = channelFlow {
        withContext(Dispatchers.IO) {
            network.run {
                connect(nodeId,
                    connect(nodeId, port).run {
                        writeL8String(Method.Schemas)
                        readL8String()
                    }
                ).run {
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

data class RpcJsonRequest(
    val method: String,
    val params: List<Any>,
    val id: Long = 0,
)
