package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIUpdate
import java.lang.StringBuilder


fun UI.EventChange.printLog() = also {
    StringBuilder().apply {
        appendLine()
        appendLine("=========")
        appendLine(event.formatLogName() + ": " + event)
        appendLine("---------")
        output.map { message ->
            val (out, value) = when (message) {
                is UI.Action -> message to message
                is UIUpdate<*, *> -> message.run { element to value }
            }
            out.formatLogName() + ": " + value
        }.forEach(this::appendLine)
        appendLine("=========")
    }.toString().let(::println)
}

fun UI.Change.printLog() = also {
    StringBuilder().apply {
        appendLine()
        appendLine("=========")
        output.map { message ->
            val (out, value) = when (message) {
                is UI.Action -> message to message
                is UIUpdate<*, *> -> message.run { element to value }
            }
            out.formatLogName() + ": " + value
        }.forEach(this::appendLine)
        appendLine("=========")
    }.toString().let(::println)
}

fun Any.formatLogName(): String = javaClass.name.split("$", limit = 2).last()
