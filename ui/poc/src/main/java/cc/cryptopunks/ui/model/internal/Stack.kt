package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.UIView
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

internal fun UI.State.resolveNextView(): List<UIView> = context.run {
    stack + requestView()
}

private fun UI.State.requestView(): UIView = context.run {
    val method = method!!
    val request = Service.Request(method.id, args)
    UIView(method, args) { repo.subscribe(request).printEach() }

}

private fun Flow<JsonNode>.printEach() = onEach { data ->
    println(Jackson.jsonPrettyWriter.writeValueAsString(data))
}

internal fun UI.State.executeCommand(): Job = context.run {
    val request = Service.Request(method!!.id, args)
    repo.execute(request)
}

internal fun UI.State.dropLastView() = stack.dropLast(1)
