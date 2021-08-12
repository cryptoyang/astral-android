package cc.cryptopunks.astral.test.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import cc.cryptopunks.astral.test.services.internal.startForegroundNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class AstralService :
    Service(),
    CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.IO) {

    private val tag = ASTRAL + "Service"
    private var binder: NetworkBinder? = null

    override fun onCreate() {
        Log.d(tag, "Starting astral service")
        startForegroundNotification()
    }

    override fun onDestroy() {
        binder = null
        cancel()
        Log.d(tag, "Destroying astral service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent): IBinder? = binder

    inner class NetworkBinder : Binder() {
        fun startServices() {
            launch {  }
        }

        fun stopServices() {
            coroutineContext.cancelChildren()
        }
    }

    companion object {
        fun intent(context: Context) = Intent(context, AstralService::class.java)
    }
}

