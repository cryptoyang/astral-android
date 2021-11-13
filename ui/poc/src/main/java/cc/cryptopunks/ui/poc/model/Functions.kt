package cc.cryptopunks.ui.poc.model

operator fun UI.State.plus(update: UIUpdate<*, *>) = UI.State(
    elements = when (update.value) {
        null -> minus(update.element)
        else -> plus(update.element to update.value)
    }
)

operator fun <E: UI.Element<T>, T> E.plus(value: T) = UIUpdate(this, value)
operator fun <E: UI.Element<T?>, T> E.unaryMinus() = UIUpdate(this, null)
operator fun <T> UIUpdate<*, *>.invoke(element: UI.Element<T>) = value as T?
