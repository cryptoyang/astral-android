package cc.cryptopunks.astral.poc

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import astralApi.Network
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AstralServiceConnection(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val onBind: suspend Network.() -> Unit,
) : ServiceConnection {

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private var job: Job? = null

    infix fun bind(context: Context) {
        context.bindService(AstralService.intent(context), this, Context.BIND_AUTO_CREATE)
    }

    infix fun unbind(context: Context) {
        job?.cancel()
        println("Service unbind")
        context.unbindService(this)
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        println("Service connected")
        job = scope.launch(dispatcher) { onBind(binder as Network) }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        job?.cancel()
        println("Service disconnected")
    }
}
