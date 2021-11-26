package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIData
import com.fasterxml.jackson.databind.JsonNode

fun UI.State.generateSelection(clicked: UI.Event.Clicked): List<UIData> =
    generateUIData(clicked.id, clicked.value)

fun UI.State.generateUIDataFromStack() = stack
    .flatMap { view -> view.args.mapKeys { (key, _) -> view.source.params[key]!! }.toList() }
    .flatMap { (type, value) -> generateUIData(type.id, value) }


fun UI.State.generateUIData(id: String, value: Any): List<UIData> =
    when (value) {
        is JsonNode -> {
            val type = requireNotNull(context.model.types[id]) {
                "Cannot resolve type for id: $id"
            }
            UIData(type, value).unfoldComplexTypes()
        }
        else -> listOf(
            UIData(Api.Type(id), value)
        )
    }

private fun UIData.unfoldComplexTypes(): List<UIData> =
    type.properties.flatMap { (key, type) ->
        when {
            type.id.isBlank() -> emptyList()
            else -> {
                val value = (value as JsonNode)[key]
                val data = UIData(type, value)
                data.unfoldComplexTypes().plus(data)
            }
        }
    } + this
