package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI

fun UI.State.addContext(other: UI.Context) = context + other

operator fun UI.Context.plus(other: UI.Context) = copy(
    methods = methods + other.methods,
    types = types + other.types,
    layouts = layouts + other.layouts,
    resolvers = resolvers + other.resolvers,
)

operator fun UI.Context.minus(other: UI.Context) = copy(
    methods = methods - other.methods.keys,
    types = types - other.types.keys,
    layouts = layouts - other.layouts.keys,
    resolvers = resolvers - other.resolvers.keys,
)
