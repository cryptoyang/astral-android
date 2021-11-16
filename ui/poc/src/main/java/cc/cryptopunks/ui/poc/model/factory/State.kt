package cc.cryptopunks.ui.poc.model.factory

import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIElement

operator fun UI.State.Companion.invoke(context: UI.Context) = UI.State(
    UIElement.Defaults
        .filter { element -> element.defaultValue != null }
        .associateWith { element -> element.defaultValue as Any }
        .plus(UI.Element.Context to context)
)
