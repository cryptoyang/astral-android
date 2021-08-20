package cc.cryptopunks.astral.node

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import astralApi.Network
import astralandroid.Astralandroid
import cc.cryptopunks.astral.node.internal.startAstral
import cc.cryptopunks.astral.node.internal.startForegroundNotification

class AstralService : Service() {

    private val tag = ASTRAL + "Service"
    private var binder: NetworkBinder? = null

    override fun onCreate() {
        Log.d(tag, "Starting astral service")
        startForegroundNotification()
        binder = NetworkBinder(startAstral())
    }

    override fun onDestroy() {
        binder = null
        Astralandroid.stop()
        Log.d(tag, "Destroying astral service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent): IBinder? = binder

    inner class NetworkBinder(network: Network) : Binder(), Network by network

    companion object {
        fun intent(context: Context) = Intent(context, AstralService::class.java)
    }
}

