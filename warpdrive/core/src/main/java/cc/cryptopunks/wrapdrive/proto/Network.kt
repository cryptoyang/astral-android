package cc.cryptopunks.wrapdrive.proto

import cc.cryptopunks.astral.enc.NetworkEncoder
import cc.cryptopunks.astral.enc.encoder
import cc.cryptopunks.astral.tcp.astralTcpNetwork

typealias Astral = NetworkEncoder

val network: Astral = astralTcpNetwork().encoder()
