package cc.cryptopunks.ui.poc.widget

import androidx.core.view.isVisible
import cc.cryptopunks.ui.poc.databinding.CommandItemBinding
import cc.cryptopunks.ui.poc.model.Service
import cc.cryptopunks.ui.poc.model.UIMethod

fun CommandItemBinding.set(item: UIMethod) {
    root.tag = item
    set(item.method, item.args)
    commandTitle.append(" " + item.score)
}

fun CommandItemBinding.set(
    method: Service.Method,
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
    commandResult.isVisible = method.result != Service.Type.Empty
    commandResult.text = method.result.formatType()
}

private fun Service.Type.formatType(): String = when {
    kind == "array" -> "Array<${properties["items"]!!.formatType()}>"
    id.isNotBlank() -> id
    else -> kind
}
