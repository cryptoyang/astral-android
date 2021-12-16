package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.UIView
import kotlinx.coroutines.Job

internal fun UI.State.resolveNextView(): List<UIView> = context.run {
    stack + requestView()
}

private fun UI.State.requestView(): UIView = context.run {
    val method = method!!
    val request = Service.Request(method.id, args)
    UIView(method, args) { repo.subscribe(request) }
}

internal fun UI.State.executeCommand(): Job = context.run {
    val request = Service.Request(method!!.id, args)
    repo.execute(request)
}

internal fun UI.State.dropLastView() = stack.dropLast(1)
