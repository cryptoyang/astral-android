package cc.cryptopunks.ui.poc.model

import cc.cryptopunks.ui.poc.mapper.camelToSnakeCase

fun UI.Context.format(method: Api.Method): String =
    formatNameFromId(method.id) + formatParams(method.params).joinToString(",", "(", ")")

private fun UI.Context.formatNameFromId(id: String) =
    id.removePrefix(model.id).replace("$", "").camelToSnakeCase()

private fun UI.Context.formatParams(params: Map<String, Api.Type>) =
    params.map { (name, type) ->
        when {
            type.id.isNotBlank() -> formatNameFromId(type.id)
            else -> name
        }
    }
