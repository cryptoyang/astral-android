package cc.cryptopunks.ui.poc.mapper.model

import cc.cryptopunks.ui.poc.api.MessengerApi
import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.mapper.openrpc.toModel
import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.schema.rpc.generateOpenRpcDocument

fun main() {
    val doc = MessengerApi.generateOpenRpcDocument()
    val model = doc.toModel()

    model.generateProteusLayouts()
        .let { Jackson.jsonPrettyWriter.writeValueAsString(it) }
        .let(::println)
}

fun Api.Model.generateProteusLayouts(): Map<String, Map<String, Any>> {

    val rootTypes: List<Api.Type> = methods.values.map { it.result }
    val subTypes = types.minus(rootTypes.map(Api.Type::id)).values

    return subTypes.toProteusLayouts(listOf("item")) + rootTypes.toProteusLayouts(emptyList())
}

fun Iterable<Api.Type>.toProteusLayouts(
    path: List<String>
): Map<String, Map<String, Any>> =
    associate { type -> type.id to type.toProteusLayout(path) }



fun Api.Type.toProteusLayout(
    path: List<String>,
): Map<String, Any> = when (type) {

    "object" -> mutableMapOf(
        "type" to "LinearLayout",
        "orientation" to "vertical",
        "layout_width" to "match_parent",
        "layout_height" to "wrap_content",
        "padding" to "8dp",
        "children" to properties.asSequence().map { (key, prop) ->
            prop.toProteusLayoutRef(path + key)
        }.toList(),
    ).also { params ->
        if (path.isNotEmpty()) params += mapOf(
            "data" to mapOf(
                "item" to path.formatRef()
            ),
            "onClick" to "item".formatRef(),
            "background" to "?android:selectableItemBackground",
        )
    }

    "array" -> mutableMapOf(
        "type" to "RecyclerView",
        "layout_width" to "match_parent",
        "layout_height" to "match_parent",
        "padding" to "8dp",
        "layout_manager" to mapOf(
            "type" to "LinearLayoutManager"
        ),
        "adapter" to mapOf(
            "@" to mapOf(
                "type" to "SimpleListAdapter",
                "item-count" to "@{items.\$length}",
                "item-layout" to properties.getValue("items")
                    .toProteusLayoutRef(path + "items") +
                        mapOf(
                            "data" to mapOf(
                                "item" to "@{items[\$index]}"
                            )
                        )
            ),
        )
    ).also { params ->
        if (path.isNotEmpty()) params += mapOf(
            "data" to mapOf(
                "items" to path.formatRef()
            ),
        )
    }

    else -> mapOf(
        "type" to "TextView",
        "layout_width" to "wrap_content",
        "layout_height" to "wrap_content",
        "text" to path.formatRef(),
    )
}

fun Api.Type.toProteusLayoutRef(
    path: List<String>,
): Map<String, Any> = when {

    type != "array" && path.isNotEmpty() && id.isNotBlank() -> mutableMapOf<String, Any>(
        "type" to "include",
        "layout" to id,
    ).also { map ->
        if (path.isNotEmpty()) map += "data" to mapOf(
            "item" to path.formatRef()
        )
    }

    else -> toProteusLayout(path)
}



private fun String.formatRef() = listOf(this).joinToString(".", "@{", "}")
private fun List<String>.formatRef() = joinToString(".", "@{", "}")
