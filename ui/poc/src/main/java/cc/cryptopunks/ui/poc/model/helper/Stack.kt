package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIRequest
import cc.cryptopunks.ui.poc.model.UIView
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

fun UI.State.resolveNextView(): List<UIView> = context.run {
    stack + requestView()
}

fun UI.State.requestView() = context.run {
    UIRequest(context, method!!, args).run {
        UIView(method, args) { requestData().printEach() }
    }
}

fun Flow<JsonNode>.printEach() = onEach { data ->
    println(Jackson.jsonPrettyWriter.writeValueAsString(data))
}

fun UI.State.executeCommand(): Unit = context.run {
    UIRequest(context, method!!, args).requestData()
}

fun UI.State.dropLastView() = stack.dropLast(1)
