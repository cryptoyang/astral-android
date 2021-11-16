package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIDisplay

fun UI.State.switchDisplay() = when (display) {
    UIDisplay.Panel -> UIDisplay.Data
    UIDisplay.Data -> UIDisplay.Panel
}

fun UI.State.displayDataOrPanel() = when {
    stack.isNotEmpty() -> UIDisplay.Data
    else -> UIDisplay.Panel
}
