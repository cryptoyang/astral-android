package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIRequest
import cc.cryptopunks.ui.poc.model.UIView

fun UI.State.resolveNextView(): List<UIView> = context.run {
    stack + UIRequest(context, method!!, args).run {
        UIView(method, args, requestData())
    }
}

fun UI.State.executeCommand(): Unit = context.run {
    UIRequest(context, method!!, args).requestData()
}

fun UI.State.dropLastView() = stack.dropLast(1)
