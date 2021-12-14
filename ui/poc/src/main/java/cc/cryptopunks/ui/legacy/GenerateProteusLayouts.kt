package cc.cryptopunks.ui.mapper.proteus

import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.service.mapper.generateOpenRpcDocument
import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.createContext
import cc.cryptopunks.ui.service.mapper.toSchema
import cc.cryptopunks.ui.service.stub.MessengerApi

fun main() {
    MessengerApi.generateOpenRpcDocument().toSchema().createContext()
        .generateProteusLayouts()
        .let(Jackson.jsonPrettyWriter::writeValueAsString)
        .let(::println)
}

fun UI.Context.generateProteusLayouts(): Map<String, Map<String, Any>> {

    val rootTypes: List<Service.Type> = methods.values.map { it.result }
    val subTypes = types.minus(rootTypes.map(Service.Type::id)).values

    return subTypes.toProteusLayouts(listOf("item")) + rootTypes.toProteusLayouts(emptyList())
}

private fun Iterable<Service.Type>.toProteusLayouts(
    path: List<String>
): Map<String, Map<String, Any>> =
    associate { type -> type.id to type.toProteusLayout(path) }


private fun Service.Type.toProteusLayout(
    path: List<String>,
): Map<String, Any> = when (kind) {

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

fun Service.Type.toProteusLayoutRef(
    path: List<String>,
): Map<String, Any> = when {

    kind != "array" && path.isNotEmpty() && id.isNotBlank() -> mutableMapOf<String, Any>(
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
