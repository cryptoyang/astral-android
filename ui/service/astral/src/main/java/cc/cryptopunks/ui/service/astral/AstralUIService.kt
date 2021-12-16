package cc.cryptopunks.ui.service.astral

import cc.cryptopunks.astral.api.*
import cc.cryptopunks.binary.bytes
import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.service.schema.OpenRpc
import cc.cryptopunks.ui.service.schema.toSchema
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class AstralUIService(
    private val network: Network
) :
    Service.Interface,
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    private val port = "ui"

    private object Method {
        val Schemas = 0.bytes
        val Request = 1.bytes
        val Subscribe = 2.bytes
    }

    override fun execute(request: Service.Request): Job = launch {
        val command = formatRpcCommand(request.method, request.args)
        network.run {
            connect("", connect("", port).run {
                write(Method.Request)
                readString { short.toInt() }
            }).run {
                writeString(command) { command.length.bytes }

            }
        }
    }

    override fun subscribe(request: Service.Request): Flow<Any> = channelFlow {
        val command = formatRpcCommand(request.method, request.args)
        withContext(Dispatchers.IO) {
            network.run {
                connect("", connect("", port).run {
                    write(Method.Subscribe)
                    readString { short.toInt() }
                }).run {
                    writeString(command) { command.length.bytes }
                    while (true) {
                        val bytes = readN(short.toInt())
                        val data = Jackson.jsonMapper.readValue<Any>(bytes)
                        send(data)
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
                connect("", connect("", port).run {
                    write(Method.Schemas)
                    readString { short.toInt() }
                }).run {
                    writeString(arg) { arg.length.bytes }
                    while (true) {
                        val bytes = readN(short.toInt())
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
