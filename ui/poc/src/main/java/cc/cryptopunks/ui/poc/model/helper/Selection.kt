package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIData
import com.fasterxml.jackson.databind.JsonNode

fun UI.State.generateSelection(clicked: UI.Event.Clicked): List<UIData> =
    when (clicked.value) {
        is JsonNode -> {
            val type = context.model.types[clicked.id]!!
            UIData(type, clicked.value).unfoldComplexTypes()
        }
        else -> listOf(
            UIData(Api.Type(clicked.id), clicked.value)
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
    }
