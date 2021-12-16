package cc.cryptopunks.ui.model

import cc.cryptopunks.ui.model.internal.createState
import cc.cryptopunks.ui.model.internal.generateLayouts
import cc.cryptopunks.ui.model.internal.resolvers

fun Service.Schema.createContext() = UI.Context(
    methods = methods,
    types = types,
    layouts = generateLayouts(),
    resolvers = resolvers(),
)

operator fun UI.State.Companion.invoke(
    repo: Service.Repo
): UI.State =
    createState(repo)
