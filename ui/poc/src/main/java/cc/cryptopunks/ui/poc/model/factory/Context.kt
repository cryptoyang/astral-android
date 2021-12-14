package cc.cryptopunks.ui.poc.model.factory

import cc.cryptopunks.ui.poc.model.Service
import cc.cryptopunks.ui.poc.model.UI

fun Service.Schema.uiContext() = UI.Context(
    methods = methods,
    types = types,
    layouts = generateLayouts(),
    resolvers = resolvers(),
)
