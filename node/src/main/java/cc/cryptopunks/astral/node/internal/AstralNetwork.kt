package cc.cryptopunks.astral.node.internal

import android.content.Context
import astralandroid.Astralandroid
import astraljava.Network
import cc.cryptopunks.astral.node.ASTRAL
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.io.File

private const val TAG = ASTRAL + "Thread"
private const val ASTRAL_DIR = "astrald"

internal fun Context.startAstral() = runBlocking {
    val network = CompletableDeferred<Network>()

    val dir = File(applicationInfo.dataDir)
        .resolve(ASTRAL_DIR)
        .apply { mkdir() }
        .absolutePath

    val runnable = Runnable {
        val multicastLock = acquireMulticastWakeLock()
        Thread.sleep(1000)
        printNetworkInterfaces()
        try {
            Astralandroid.register("Android hijack astral network", network::complete)
            Astralandroid.start(dir)
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            multicastLock.release()
        }
    }
    Thread(runnable, TAG).start()
    network.await()
}
