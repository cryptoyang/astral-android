package cc.cryptopunks.ui.model.internal

import cc.cryptopunks.ui.model.Service

val Service.Method.hasResult get() = result != Service.Type.Empty
