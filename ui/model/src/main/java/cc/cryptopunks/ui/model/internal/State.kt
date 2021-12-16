package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.UIElement

internal fun createState(repo: Service.Repo) = UI.State(
    UIElement.Defaults
        .filter { element -> element.defaultValue != null }
        .associateWith { element -> element.defaultValue as Any }
        .plus(UI.Element.Repo to repo)
)

internal fun UI.State.plus(vararg updates: UI.Update<*, *>): UI.State =
    this + updates.toList()

internal operator fun UI.State.plus(updates: List<UI.Update<*, *>>): UI.State =
    updates.fold(this) { acc, update -> acc + update }

internal operator fun UI.State.plus(update: UI.Update<*, *>) = UI.State(
    elements = when (update.value) {
        null -> minus(update.element)
        else -> plus(update.element to update.value)
    }
)
