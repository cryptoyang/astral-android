package cc.cryptopunks.ui.poc.model.helper

import cc.cryptopunks.ui.poc.model.Api

val Api.Method.hasResult get() = result != Api.Type.Empty
