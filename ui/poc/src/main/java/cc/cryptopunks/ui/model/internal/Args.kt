package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.*

internal val UI.State.textIsRequiredArg get() = isRequiredArg(text)

private fun UI.State.isRequiredArg(text: String): Boolean =
    text.isNotBlank() && null != when (param?.type?.kind) {
        "string" -> text
        "boolean" -> text.toBooleanStrictOrNull()
        "integer" -> text.toIntOrNull()
        "number" -> text.toDoubleOrNull()
        else -> null
    }

internal fun UI.State.argDataFromSelection(): UIData? =
    selection.firstOrNull { data -> isRequiredArg(data) }

private fun UI.State.isRequiredArg(data: UIData): Boolean =
    when (data.type.kind) {
        Service.Type.obj -> data.type.id == param?.type?.id
        else -> data.type.kind == param?.type?.kind
    }

internal fun UI.State.argsWith(arg: Pair<String, Any>): UIArgs = args + arg

internal fun UI.State.argsWith(data: UIData): UIArgs = argsWith(data.value)

private fun UI.State.argsWith(value: Any): UIArgs = args + (param!!.name to value)

internal fun UI.State.dropLastArg(): UIArgs = args - args.keys.last()

internal fun UI.State.matchedArgs(): UIArgs =
    when (val method = method) {
        null -> emptyMap()
        else -> methods.firstOrNull { it.method.id == method.id }
            ?.args
            ?: emptyMap()
    }

internal fun UIMethod.args(): UIArgs = elements
    .filter { it.type is UIMethod.Type.ArgValue }
    .filter { it.value !is UIMethod.Type.Unknown }
    .associate { (it.type as UIMethod.Type.ArgValue).name to it.value }

internal val UI.Action.SetArg.arg get() = key to value
