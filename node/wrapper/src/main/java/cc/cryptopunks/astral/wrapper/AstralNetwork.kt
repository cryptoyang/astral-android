package cc.cryptopunks.astral.wrapper

import android.content.Context
import android.util.Log
import astralandroid.Astralandroid
import cc.cryptopunks.astral.api.Network
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.Executors

const val ASTRAL = "Astral"
private const val ASTRAL_DIR = "astrald"

private val astralScope = CoroutineScope(
    SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher()
)
private var astralJob: Job? = null

fun Context.startAstral(): Network = runBlocking {
    val network = CompletableDeferred<astralApi.Network>()

    val dir = File(applicationInfo.dataDir)
        .resolve(ASTRAL_DIR)
        .apply { mkdir() }
        .absolutePath

    astralJob = astralScope.launch {
        val multicastLock = acquireMulticastWakeLock()
        try {
            Astralandroid.register("Android hijack astral network", network::complete)
            Astralandroid.start(dir)
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            Log.d("AstralNetwork", "releasing multicast")
            multicastLock.release()
        }
    }
    NetworkAdapter(network.await())
}

fun stopAstral() = runBlocking {
    Astralandroid.stop()
    astralJob?.join()
}
