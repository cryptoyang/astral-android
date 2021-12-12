package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.Service

val Service.Method.hasResult get() = result != Service.Type.Empty
