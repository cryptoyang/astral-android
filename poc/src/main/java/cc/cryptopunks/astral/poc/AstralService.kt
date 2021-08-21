package cc.cryptopunks.astral.poc

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import astralApi.Network

class AstralService : Service() {

    private val tag = javaClass.simpleName
    private val binder by lazy { NetworkBinder(startAstral()) }

    override fun onCreate() {
        startAstralForeground()
        Log.d(tag, "Starting astrald")
        binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onBind(intent: Intent): IBinder {
        Log.d(tag, "Binding $intent")
        return binder
    }

    override fun onDestroy() {
        Log.d(tag, "Destroying astrald")
    }

    companion object {
        fun intent(context: Context) = Intent(context, AstralService::class.java)
    }

    class NetworkBinder(network: Network) : Binder(), Network by network
}

