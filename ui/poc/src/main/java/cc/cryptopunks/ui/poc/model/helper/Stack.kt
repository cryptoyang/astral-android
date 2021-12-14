package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.model.Service
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIView
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

fun UI.State.resolveNextView(): List<UIView> = context.run {
    stack + requestView()
}

fun UI.State.requestView() = context.run {
    val method = method!!
    val request = Service.Request(method.id, args)
    UIView(method, args) { repo.subscribe(request).printEach() }

}

fun Flow<JsonNode>.printEach() = onEach { data ->
    println(Jackson.jsonPrettyWriter.writeValueAsString(data))
}

fun UI.State.executeCommand(): Job = context.run {
    val request = Service.Request(method!!.id, args)
    repo.execute(request)
}

fun UI.State.dropLastView() = stack.dropLast(1)
