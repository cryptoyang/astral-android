package cc.cryptopunks.ui.poc.model2

import cc.cryptopunks.ui.poc.api.MessengerApi
import cc.cryptopunks.ui.poc.schema.rpc.generateOpenRpcDocument

fun main() {
    val doc = MessengerApi.generateOpenRpcDocument()
    val context = UI.Context(doc)
    val state = UI.State(context)
    val handleEvent = eventHandler(state)
    val changes = mutableListOf<UI.Change>()

    changes += handleEvent(UI.Event.Init)
    changes += handleEvent(UI.Event.Method(changes.first().state.methods.last().method))
    changes += handleEvent(UI.Event.Action)

    println(changes)
}

