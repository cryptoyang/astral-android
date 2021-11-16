package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.*

fun UI.State.resolveNextView(): List<UIView> = context.run {
    stack + UIRequest(context, method!!, args).run {
        UIView(method, args, requestData())
    }
}

fun UI.State.dropLastView() = stack.dropLast(1)
