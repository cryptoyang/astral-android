package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.UIUpdate
import java.lang.StringBuilder

internal fun UI.Change.printLog() = also {
    StringBuilder().apply {
        appendLine()
        appendLine("=========")
        output.map { message ->
            val (out, value) = when (message) {
                is UI.Event,
                is UI.Action -> message to message
                is UIUpdate<*, *> -> message.run { element to value }
            }
            out.formatLogName() + ": " + value
        }.forEach(this::appendLine)
        appendLine("=========")
    }.toString().let(::println)
}

private fun Any.formatLogName(): String = javaClass.name.split("$", limit = 2).last()
