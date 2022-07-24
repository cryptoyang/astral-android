package cc.cryptopunks.wrapdrive.api

import cc.cryptopunks.astral.enc.NetworkEncoder
import cc.cryptopunks.astral.enc.encoder
import cc.cryptopunks.astral.tcp.astralTcpNetwork

typealias Astral = NetworkEncoder

val network: Astral = astralTcpNetwork().encoder()
