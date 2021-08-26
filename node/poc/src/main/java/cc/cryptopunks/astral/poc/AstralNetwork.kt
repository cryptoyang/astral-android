package cc.cryptopunks.astral.poc

import android.content.Context
import astralandroid.Astralandroid
import astralApi.Network
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.io.File

fun Context.startAstral(): Network = runBlocking {

    val dir = File(applicationInfo.dataDir)
        .resolve("astrald")
        .apply { mkdir() }
        .absolutePath

    val network = CompletableDeferred<Network>()

    Thread {
        println("registering android")
        Astralandroid.register("android", network::complete)
        Astralandroid.start(dir)
    }.start()

    network.await()
}
