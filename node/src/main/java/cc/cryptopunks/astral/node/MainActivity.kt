package cc.cryptopunks.astral.node

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import astralandroid.Astralandroid
import cc.cryptopunks.astral.node.internal.copyToClipboard
import cc.cryptopunks.astral.node.internal.formatAstralLogs
import cc.cryptopunks.astral.node.internal.logcatFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val nodeIdTextView: TextView by lazy { findViewById(R.id.nodeIdTestView) }
    private val logTextView: TextView by lazy { findViewById(R.id.logTextView) }
    private val restartButton: TextView by lazy { findViewById(R.id.restartServiceButton) }
    private var binder: AstralService.NetworkBinder? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = service as AstralService.NetworkBinder
            nodeIdTextView.text = service.identity()
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
        nodeIdTextView.setOnLongClickListener {
            copyToClipboard(nodeIdTextView.text.toString())
            Toast.makeText(this, "Id copied to clipboard.", Toast.LENGTH_SHORT).show()
            true
        }
        restartButton.setOnClickListener {
            Astralandroid.stop()
        }
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy")
        cancel()
        super.onDestroy()
    }
}
