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
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val scope = MainScope()

    private val remoteIdEditText: EditText by lazy { findViewById(R.id.remoteIdEditText) }
    private val portEditText: EditText by lazy { findViewById(R.id.portEditText) }
    private val connectButton: Button by lazy { findViewById(R.id.connectButton) }
    private val disconnectButton: Button by lazy { findViewById(R.id.disconnectButton) }
    private val statusTextView: TextView by lazy { findViewById(R.id.statusTextView) }
    private val messageEditText: EditText by lazy { findViewById(R.id.messageEditText) }
    private val sendButton: Button by lazy { findViewById(R.id.sendButton) }
    private var stream: Stream? = null

    private val sharedPrefs by lazy { getSharedPreferences("astral.connect", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPrefs.run {
            remoteIdEditText.setText(getString("connect-id", null))
            portEditText.setText(getString("connect-port", null))
        }
        connectButton.setOnClickListener {
            disconnect()

            scope.launch(Dispatchers.IO) {
                try {
                    statusTextView.text = null
                    sharedPrefs.edit().apply {
                        putString("connect-id", remoteIdEditText.text.toString())
                        putString("connect-port", portEditText.text.toString())
                    }.apply()
                    stream = astralTcpNetwork(
                        encode = GsonEncoder(),
                        decode = GsonDecoder(),
                    ).connect(
                        identity = remoteIdEditText.text.toString(),
                        port = portEditText.text.toString()
                    )
                    withContext(Dispatchers.Main) {
                        sendButton.isEnabled = true
                        disconnectButton.isEnabled = true
                    }
                    Log.d("MainActivity", "connected")
                } catch (e: Throwable) {
                    statusTextView.text = e.message
                    e.printStackTrace()
                }
            }
        }
        disconnectButton.setOnClickListener {
            disconnect()
        }
        sendButton.setOnClickListener {
            stream?.let { stream ->
                scope.launch(Dispatchers.IO) {
                    Log.d("MainActivity", "sending")
                    stream.write(listOf<Byte>(2).toByteArray())
                    stream.write(messageEditText.text.toString().toByteArray())
                    Log.d("MainActivity", "sent")
                }
            } ?: Toast
                .makeText(this@MainActivity, "Stream not connected", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun disconnect() {
        stream?.close()
        stream = null
        sendButton.isEnabled = false
        disconnectButton.isEnabled = false
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }
}
