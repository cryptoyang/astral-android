package cc.cryptopunks.ui.poc.model2

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

sealed class UIElement<T>(
    private val getDefault: () -> T
) : UI.Element<T> {
    constructor(default: T) : this({ default })

    override val defaultValue get() = getDefault()
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
