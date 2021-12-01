package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIDisplay

fun UI.State.switchDisplay() = when {
    UIDisplay.Panel in display -> display - UIDisplay.Panel + UIDisplay.Data
    UIDisplay.Data in display -> display - UIDisplay.Data + UIDisplay.Panel
    else -> display
}

fun UI.State.displayPanel() = display - UIDisplay.Data + UIDisplay.Panel

fun UI.State.displayData() = display - UIDisplay.Panel + UIDisplay.Data

fun UI.State.defaultDisplay() = when {
    stack.isNotEmpty() -> setOf(UIDisplay.Data)
    else -> setOf(UIDisplay.Panel)
}
