package cc.cryptopunks.astral.wrapdrive

import cc.cryptopunks.astral.client.astralTcpNetwork
import cc.cryptopunks.astral.coder.GsonDecoder
import cc.cryptopunks.astral.coder.GsonEncoder

val network = astralTcpNetwork(GsonEncoder(), GsonDecoder())
