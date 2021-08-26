package cc.cryptopunks.astral.wrapper

import android.content.Context
import astralandroid.Astralandroid
import cc.cryptopunks.astral.api.Network
import cc.cryptopunks.astral.node.core.NetworkAdapter
import cc.cryptopunks.astral.node.core.acquireMulticastWakeLock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.io.File

const val ASTRAL = "Astral"
private const val TAG = ASTRAL + "Thread"
private const val ASTRAL_DIR = "astrald"

fun Context.startAstral(): Network = runBlocking {
    val network = CompletableDeferred<astralApi.Network>()

    val dir = File(applicationInfo.dataDir)
        .resolve(ASTRAL_DIR)
        .apply { mkdir() }
        .absolutePath

    val runnable = Runnable {
        val multicastLock = acquireMulticastWakeLock()
        Thread.sleep(1000)
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
    NetworkAdapter(network.await())
}

fun stopAstral() {
    Astralandroid.stop()
}
