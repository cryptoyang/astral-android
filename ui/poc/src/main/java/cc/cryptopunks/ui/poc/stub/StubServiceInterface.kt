package cc.cryptopunks.ui.poc.stub

import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.mapper.openrpc.toSchema
import cc.cryptopunks.ui.poc.model.Service
import cc.cryptopunks.ui.poc.transport.schema.rpc.generateOpenRpcDocument
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class StubServiceInterface : Service.Interface {

    override fun schemas(known: Set<Service.Schema.Version>): Flow<Service.Schema> = flow {
        val doc = MessengerApi.generateOpenRpcDocument()
        val schema = doc.toSchema()
        emit(schema)
    }

    override fun status(): Flow<Service.Status> {
        TODO("Not yet implemented")
    }

    override fun execute(request: Service.Request): Job {
        return stubServiceScope.launch {
            val method = parseRpcMethod(request.method, request.args)
            handle(method).collect()
        }
    }

    override fun subscribe(request: Service.Request): Flow<JsonNode> {
        val method = parseRpcMethod(request.method, request.args)
        return handle(method).map { Jackson.jsonSlimMapper.valueToTree(it) }
    }
}
