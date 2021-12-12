package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.*

fun UI.State.nextParam(): UIParam? =
    context.nextParam(stack, method, args)

fun UI.Context.nextParam(
    stack: List<UIView>,
    method: Service.Method?,
    args: UIArgs
): UIParam? =
    if (method == null) null
    else method.params.minus(args.keys)
        .toList().firstOrNull()
        ?.let { (name, type) ->
            UIParam(
                name = name,
                type = type,
                resolvers = resolvers(stack, type)
            )
        }

private fun UI.Context.resolvers(
    stack: List<UIView>,
    type: Service.Type
): Iterable<UIResolver> {

    val defaultResolvers = resolvers[type.id]
        ?: resolvers[type.kind]
        ?: throw IllegalArgumentException("no resolver $resolvers for $type")

    val additionalResolvers = listOfNotNull(
        stack.lastOrNull()
            ?.takeIf { view ->
                defaultResolvers
                    .filterIsInstance<UIResolver.Method>()
                    .any { method -> method.method.id == view.source.id }
            }
            ?.let { view ->
                UIResolver.Data(view)
            }
    )

    return defaultResolvers + additionalResolvers
}
