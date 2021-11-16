package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.UI

fun UI.State.isReady() = args.keys == method?.params?.keys
