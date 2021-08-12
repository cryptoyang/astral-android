package cc.cryptopunks.astral.test.services

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.astral.test.services.internal.formatAstralLogs
import cc.cryptopunks.astral.test.services.internal.logcatFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val logTextView: TextView by lazy { findViewById(R.id.logTextView) }
    private val startButton: Button by lazy { findViewById(R.id.startButton) }
    private val stopButton: Button by lazy { findViewById(R.id.startButton) }

    private var binder: AstralService.NetworkBinder? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = service as AstralService.NetworkBinder
        }

        override fun onServiceDisconnected(name: ComponentName) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        launch(Dispatchers.IO) {
            logcatFlow().formatAstralLogs().collect { log ->
                withContext(Dispatchers.Main) {
                    logTextView.append(log)
                }
            }
        }
        startForegroundService(AstralService.intent(this))
        bindService(AstralService.intent(this), connection, Context.BIND_AUTO_CREATE)
        startButton.setOnClickListener { binder?.startServices() }
        stopButton.setOnClickListener { binder?.stopServices() }
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy")
        cancel()
        super.onDestroy()
    }
}
