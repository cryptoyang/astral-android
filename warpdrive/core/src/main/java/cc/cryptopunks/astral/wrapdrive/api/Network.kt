package cc.cryptopunks.astral.wrapdrive.api

import cc.cryptopunks.astral.gson.GsonCoder
import cc.cryptopunks.astral.tcp.astralTcpNetwork

val network = astralTcpNetwork(GsonCoder())
