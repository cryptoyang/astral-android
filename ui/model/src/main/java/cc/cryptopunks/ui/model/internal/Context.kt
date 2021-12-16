package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.UI

internal fun UI.State.addContext(other: UI.Context) = context + other

private operator fun UI.Context.plus(other: UI.Context) = copy(
    methods = methods + other.methods,
    types = types + other.types,
    layouts = layouts + other.layouts,
    resolvers = resolvers + other.resolvers,
)

private operator fun UI.Context.minus(other: UI.Context) = copy(
    methods = methods - other.methods.keys,
    types = types - other.types.keys,
    layouts = layouts - other.layouts.keys,
    resolvers = resolvers - other.resolvers.keys,
)
