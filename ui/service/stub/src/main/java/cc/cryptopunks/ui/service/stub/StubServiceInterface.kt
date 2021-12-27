package cc.cryptopunks.ui.service.stub

import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.service.base.generateOpenRpcDocument
import cc.cryptopunks.ui.service.schema.toSchema
import cc.cryptopunks.ui.testing.MessengerApi
import com.fasterxml.jackson.module.kotlin.convertValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
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

    override fun subscribe(request: Service.Request): Flow<Any> {
        val method = parseRpcCommand(request.method, request.args)
        return handle(method).map {
            Jackson.jsonSlimMapper.convertValue<Any>(it)
        }.onEach {
            println(it)
        }
    }
}
