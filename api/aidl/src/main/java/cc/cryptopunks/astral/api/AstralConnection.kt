package cc.cryptopunks.astral.api

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AstralConnection<T>(
    private val convert: (IBinder) -> T,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val onBind: suspend T.() -> Unit,
) : ServiceConnection {

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private var job: Job? = null

    infix fun bind(context: Context) {
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    infix fun unbind(context: Context) {
        job?.cancel()
        println("Service unbind")
        context.unbindService(this)
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        println("Service connected")
        val proxy = convert(binder)
        job = scope.launch(dispatcher) { onBind(proxy) }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        job?.cancel()
        println("Service disconnected")
    }

    private companion object {
        val intent: Intent
            get() = Intent().apply {
                setClassName(
                    "cc.cryptopunks.astral.service",
                    "cc.cryptopunks.astral.service.AstralService"
                )
            }
    }
}
