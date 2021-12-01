package cc.cryptopunks.ui.poc.model

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

typealias UIElements = Map<UI.Element<*>, Any>

sealed class UIElement<T>(
    private val getDefault: () -> T = GetDefault
) : UI.Element<T> {
    constructor(default: T) : this({ default })

    override val defaultValue get() = getDefault()

    init {
        if (getDefault != GetDefault)
            defaults += this
    }

    internal companion object {
        private val GetDefault: () -> Nothing = ::TODO
        private val defaults = mutableSetOf<UIElement<*>>()
        init {
            UI.Element::class.nestedClasses.forEach {
                it.objectInstance
            }
        }

        val Defaults: Set<UIElement<*>> get() = defaults
    }
}

abstract class UIState(elements: UIElements) : UIElements by elements {
    operator fun <T> UI.Element<T>.unaryPlus(): ReadOnlyProperty<UIElements, T> =
        UIStateProperty(this)
}

private class UIStateProperty<T>(private val element: UI.Element<T>) :
    ReadOnlyProperty<UIElements, T> {

    private var isInitialized: Boolean = false
    private var value: T? = null

    override fun getValue(thisRef: UIElements, property: KProperty<*>): T =
        if (isInitialized) value as T
        else (thisRef[element] as T).also {
            isInitialized = true
            value = it
        }
}

data class UIUpdate<E : UI.Element<T>, T>(val element: E, val value: T) : UIMessage

sealed interface UIMessage

val UIMessage.output get() = when(this) {
    is UIUpdate<*, *> -> element
    is UI.Action -> this
}
