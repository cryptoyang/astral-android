package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIMessage
import cc.cryptopunks.ui.poc.model.UIUpdate

val UIMessage.output
    get() = when (this) {
        is UIUpdate<*, *> -> element
        is UI.Action -> this
    }
