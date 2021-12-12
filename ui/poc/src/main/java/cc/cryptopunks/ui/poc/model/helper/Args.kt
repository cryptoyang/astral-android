package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.Service
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIData

val UI.State.textIsRequiredArg get() = isRequiredArg(text)

fun UI.State.isRequiredArg(text: String): Boolean =
    text.isNotBlank() && null != when (param?.type?.kind) {
        "string" -> text
        "boolean" -> text.toBooleanStrictOrNull()
        "integer" -> text.toIntOrNull()
        "number" -> text.toDoubleOrNull()
        else -> null
    }

fun UI.State.isRequiredArg(data: UIData): Boolean = when {
    data.type.kind == Service.Type.obj -> data.type.id == param?.type?.id
    else -> data.type.kind == param?.type?.kind
}

fun UI.State.argDataFromSelection() =
    selection.firstOrNull { data -> isRequiredArg(data) }

fun UI.State.argsWith(arg: Pair<String, Any>) = args + arg

fun UI.State.argsWith(value: Any) = args + (param!!.name to value)

fun UI.State.argsWith(data: UIData) = argsWith(data.value)

fun UI.State.dropLastArg() = args - args.keys.last()

fun UI.State.defaultArgs(): Map<String, Any> =
    when (val method = method) {
        null -> emptyMap()
        else -> selection.toMutableList().run {
            method.params.toList().mapNotNull { (key, type) ->
                firstOrNull { clicked -> clicked.type.id == type.id }?.let { matching ->
                    remove(matching)
                    key to matching.value
                }
            }.toMap()
        }
    }

fun UI.State.matchedArgs() =
    when (val method = method) {
        null -> emptyMap()
        else -> methods.firstOrNull { it.method.id == method.id }
            ?.args
            ?: emptyMap()
    }
