package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.UIMessage
import cc.cryptopunks.ui.model.UIUpdate

val UIMessage.output
    get() = when (this) {
        is UIUpdate<*, *> -> element
        is UI.Action -> this
    }
