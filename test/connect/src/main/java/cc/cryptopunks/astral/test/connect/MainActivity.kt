package cc.cryptopunks.astral.test.connect

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.astral.api.Stream
import cc.cryptopunks.astral.client.astralTcpNetwork
import cc.cryptopunks.astral.coder.GsonDecoder
import cc.cryptopunks.astral.coder.GsonEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val scope = MainScope()

    private val localIdEditText: EditText by lazy { findViewById(R.id.localIdEditText) }
    private val remoteIdEditText: EditText by lazy { findViewById(R.id.remoteIdEditText) }
    private val portEditText: EditText by lazy { findViewById(R.id.portEditText) }
    private val connectButton: Button by lazy { findViewById(R.id.connectButton) }
    private val statusTextView: TextView by lazy { findViewById(R.id.statusTextView) }
    private val messageEditText: EditText by lazy { findViewById(R.id.messageEditText) }
    private val sendButton: Button by lazy { findViewById(R.id.sendButton) }
    private var stream: Stream? = null

    private val sharedPrefs by lazy { getSharedPreferences("astral.connect", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPrefs.run {
            localIdEditText.setText(getString("id", null))
            remoteIdEditText.setText(getString("connect-id", null))
            portEditText.setText(getString("connect-port", null))
        }
        connectButton.setOnClickListener {
            stream?.close()
            stream = null

            scope.launch(Dispatchers.IO) {
                try {
                    statusTextView.text = null
                    sharedPrefs.edit().apply {
                        putString("id", localIdEditText.text.toString())
                        putString("connect-id", remoteIdEditText.text.toString())
                        putString("connect-port", portEditText.text.toString())
                    }.apply()
                    stream = astralTcpNetwork(
                        identity = localIdEditText.text.toString(),
                        encode = GsonEncoder(),
                        decode = GsonDecoder(),
                    ).connect(
                        identity = remoteIdEditText.text.toString(),
                        port = portEditText.text.toString()
                    )
                    Log.d("MainActivity", "connected")
                    val s = stream
                    println(s)
                } catch (e: Throwable) {
                    statusTextView.text = e.message
                    e.printStackTrace()
                }
            }
        }
        sendButton.setOnClickListener {
            stream?.let { stream ->
                scope.launch(Dispatchers.IO) {
                    Log.d("MainActivity", "sending")
                    stream.write(messageEditText.text.toString().toByteArray())
                    Log.d("MainActivity", "sent")
                }
            } ?: Toast
                .makeText(this@MainActivity, "Stream not connected", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroy() {
        stream?.close()
        stream = null
        super.onDestroy()
    }
}
