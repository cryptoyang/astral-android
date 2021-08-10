package cc.cryptopunks.astral.node.internal

import android.util.Log
import java.net.NetworkInterface

internal fun printNetworkInterfaces() = NetworkInterface
    .getNetworkInterfaces().toList()
    .forEach { Log.d("AstralNetwork", "net interface: $it, ${it.interfaceAddresses}") }
