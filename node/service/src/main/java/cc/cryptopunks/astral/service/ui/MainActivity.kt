package cc.cryptopunks.astral.service.ui

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.astral.service.AstralService
import cc.cryptopunks.astral.service.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val nodeIdTextView: TextView by lazy { findViewById(R.id.nodeIdTestView) }
    private val scrollView: ScrollView by lazy { findViewById(R.id.scrollView) }
    private val logTextView: TextView by lazy { findViewById(R.id.logTextView) }
    private val startButton: TextView by lazy { findViewById(R.id.startServiceButton) }
    private val killButton: TextView by lazy { findViewById(R.id.killServiceButton) }
    private val serviceIntent by lazy { AstralService.intent(this) }
    private var binder: AstralService.NetworkBinder? by Delegates.observable(null) { _, _, next ->
        startButton.isEnabled = next == null
        killButton.isEnabled = next != null
    }
    private var logcatJob: Job? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = service as AstralService.NetworkBinder
            nodeIdTextView.text = service.identity()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Toast.makeText(this@MainActivity, "Service stopped.", Toast.LENGTH_SHORT).show()
            binder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindAstralService()
        startAstralService()
        nodeIdTextView.setOnLongClickListener { copyNodeId(); true }
        killButton.setOnClickListener { unbindAstralService(); stopAstralService() }
        startButton.setOnClickListener { startAstralService(); bindAstralService() }
    }

    override fun onStart() {
        super.onStart()
        logcatJob = launch {
            logcatCacheFlow().collect { log ->
                withContext(Dispatchers.Main) {
                    logTextView.append(log)
                }
                scrollView.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        logcatJob?.cancel()
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy")
        cancel()
        super.onDestroy()
    }

    private fun copyNodeId() {
        copyToClipboard(nodeIdTextView.text.toString())
        Toast.makeText(this, "Id copied to clipboard.", Toast.LENGTH_SHORT).show()
    }

    private fun bindAstralService() {
        binder ?: bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun startAstralService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(serviceIntent) else
            startService(serviceIntent)
    }

    private fun stopAstralService() {
        stopService(serviceIntent)
    }

    private fun unbindAstralService() {
        binder ?: return
        unbindService(connection)
        binder = null
    }
}
