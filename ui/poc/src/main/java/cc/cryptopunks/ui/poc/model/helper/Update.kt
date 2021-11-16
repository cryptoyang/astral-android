package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIMessage
import cc.cryptopunks.ui.poc.model.UIUpdate

fun UI.State.update(message: UIMessage) = when (message) {
    is UIUpdate<*, *> -> plus(message)
    else -> this
}

operator fun UI.State.plus(update: UIUpdate<*, *>) = UI.State(
    elements = when (update.value) {
        null -> minus(update.element)
        else -> plus(update.element to update.value)
    }
)

operator fun <E: UI.Element<T>, T> E.plus(value: T) = UIUpdate(this, value)
operator fun <E: UI.Element<T?>, T> E.unaryMinus() = UIUpdate(this, null)
operator fun <T> UIUpdate<*, *>.invoke(element: UI.Element<T>): T? = value as T?

fun <E : UI.Element<T>, T> E.default() = UIUpdate(this, defaultValue)
