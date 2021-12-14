package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.UIUpdate

internal operator fun <E : UI.Element<T>, T> E.plus(value: T) = UIUpdate(this, value)

internal fun <E : UI.Element<T>, T> E.empty() = UIUpdate(this, defaultValue)
