package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.UI

val UI.Message.output: UI.Output
    get() = when (this) {
        is UI.Update<*, *> -> element
        is UI.Action -> this
    }
