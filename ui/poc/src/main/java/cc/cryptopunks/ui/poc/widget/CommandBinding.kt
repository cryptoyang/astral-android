package cc.cryptopunks.ui.poc.widget

import androidx.core.view.isVisible
import cc.cryptopunks.ui.poc.databinding.CommandItemBinding
import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.UIMethod

fun CommandItemBinding.set(item: UIMethod) {
    root.tag = item
    set(item.method, item.args)
    commandTitle.append(" " + item.score)
}

fun CommandItemBinding.set(
    method: Api.Method,
    args: Map<String, Any> = emptyMap()
) {
    commandTitle.text = method.id
    commandParams.isVisible = method.params.isNotEmpty()
    commandParams.text = method.params
        .map { (name, apiType) ->
            val type = apiType.formatType().let { ": $it" }
            val value = args[name]?.let { " = $it" } ?: ""
            name + type + value
        }
        .joinToString("\n")
    commandResult.isVisible = method.result != Api.Type.Empty
    commandResult.text = method.result.formatType()
}

private fun Api.Type.formatType(): String = when {
    type == "array" -> "Array<${properties["items"]!!.formatType()}>"
    id.isNotBlank() -> id
    else -> type
}
