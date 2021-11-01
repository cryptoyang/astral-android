package cc.cryptopunks.ui.poc.widget

import androidx.core.view.isVisible
import cc.cryptopunks.ui.poc.databinding.CommandItemBinding
import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.Score

fun CommandItemBinding.set(item: Score) {
    root.tag = item
    set(item.method)
    commandTitle.append(" " + item.score)
}

fun CommandItemBinding.set(
    method: Api.Method,
    args: Map<String, String> = emptyMap()
) {
    commandTitle.text = method.id
    commandParams.isVisible = method.params.isNotEmpty()
    commandParams.text = method.params
        .map { (name, type) ->
            val suffix = args[name]?.let { " = $it" }
                ?: type.formatType().let { ": $it" }
            name + suffix
        }
        .joinToString("\n")
    commandResult.isVisible = method.result != Api.Type.Empty
    commandResult.text = method.result.formatType()
}

private fun Api.Type.formatType(): String = when {
    type == "array" -> "Array<${properties["item"]!!.formatType()}>"
    id.isNotBlank() -> id
    else -> type
}
