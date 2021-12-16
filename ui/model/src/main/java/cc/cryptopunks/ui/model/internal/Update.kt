package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.UI

internal operator fun <E : UI.Element<T>, T> E.plus(value: T) = UI.Update(this, value)

internal fun <E : UI.Element<T>, T> E.empty() = UI.Update(this, defaultValue)
