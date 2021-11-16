package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIParam
import cc.cryptopunks.ui.poc.model.UIResolver

fun UI.State.nextParam() =
    (method?.params ?: emptyMap())
        .minus(args.keys).toList().firstOrNull()
        ?.let { (name, type) ->
            UIParam(
                name = name,
                type = type,
                resolvers = context.resolvers[type.id]
                    ?: context.resolvers[type.type]
                    ?: throw IllegalArgumentException("no resolver ${context.resolvers} for $type")
            ).let { param ->
                val next = updateResolvers(param)
                next
            }
        }


private fun UI.State.updateResolvers(param: UIParam): UIParam {

    val view = stack.lastOrNull() ?: return param

    param.resolvers
        .filterIsInstance<UIResolver.Method>()
        .any { method -> method.method.id == view.source.id } || return param

    return param.copy(
        resolvers = param.resolvers + UIResolver.Data(view)
    )
}
