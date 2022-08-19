package cc.cryptopunks.wrapdrive.proto

import cc.cryptopunks.astral.client.enc.NetworkEncoder
import cc.cryptopunks.astral.client.enc.encoder
import cc.cryptopunks.astral.client.tcp.astralTcpNetwork

typealias Astral = NetworkEncoder

val network: Astral = astralTcpNetwork().encoder()
