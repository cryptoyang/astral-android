package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI

fun UI.State.isRequiredArg(text: String): Boolean =
    null != when (param?.type?.type) {
        "string" -> text
        "boolean" -> text.toBooleanStrictOrNull()
        "integer" -> text.toIntOrNull()
        "number" -> text.toDoubleOrNull()
        else -> null
    }

fun UI.State.isRequiredArg(clicked: UI.Event.Clicked): Boolean =
    clicked.id == param?.type?.id || clicked.id == param?.type?.type

fun UI.State.nextArg(value: Any) = args + (param!!.name to value)

fun UI.State.dropLastArg() = args - args.keys.last()

fun UI.State.defaultArgs(): Map<String, Any> =
    when (val method = method) {
        null -> emptyMap()
        else -> selection.toMutableList().run {
            method.params.toList().mapNotNull { (key, type) ->
                firstOrNull { clicked -> clicked.id == type.id }?.let { matching ->
                    remove(matching)
                    key to matching.value
                }
            }.toMap()
        }
    }
