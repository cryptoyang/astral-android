package cc.cryptopunks.ui.service.stub

import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.service.mapper.generateOpenRpcDocument
import cc.cryptopunks.ui.service.mapper.toSchema
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
            val method = parseRpcCommand(request.method, request.args)
            handle(method).collect()
        }
    }

    override fun subscribe(request: Service.Request): Flow<JsonNode> {
        val method = parseRpcCommand(request.method, request.args)
        return handle(method).map { Jackson.jsonSlimMapper.valueToTree(it) }
    }
}
