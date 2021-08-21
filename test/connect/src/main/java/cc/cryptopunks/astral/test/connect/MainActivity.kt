package cc.cryptopunks.astral.test.connect

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import binary.Binary
import cc.cryptopunks.astral.api.Stream
import cc.cryptopunks.astral.client.astralTcpNetwork
import cc.cryptopunks.astral.coder.GsonDecoder
import cc.cryptopunks.astral.coder.GsonEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import msg.Message
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.experimental.and

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
//                    val recipient = remoteIdEditText.text.toString()
                    val recipient =
                        "033c352b239deb28292d48f36e742e8b84ba60ad1abdcc29c669883836203f6b3a"
                    val message = messageEditText.text.toString()
                    stream.write(Binary.int16UBytes(1))

                    val msg = Message("", recipient, message).pack()
//                    stream.write(intToUInt32(msg.size))
                    stream.write(msg)
                    Log.d("MainActivity", "sent")
                    val wait = ByteArray(1)
                    stream.read(wait)
                    withContext(Dispatchers.Main) {
                        Log.d("MainActivity", "disconnecting")
                        disconnect()
                    }
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

private fun Long.toUInt64Bytes(): ByteArray {
    val bytes = ByteArray(Long.SIZE_BYTES)
    ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).putLong(this and 0xff)
    return bytes
}

private fun Int.toUInt32Bytes(): ByteArray {
    val bytes = ByteArray(Long.SIZE_BYTES)
    ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).putInt(this and 0xffff)
    return bytes
}

private fun intToUInt32(value: Int): ByteArray {
    val bytes = ByteArray(Int.SIZE_BYTES)
    ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).putInt(value and 0xff)
    return bytes
}

private fun intToUInt16(value: Short): ByteArray = ByteBuffer
    .allocate(Short.SIZE_BYTES)
    .order(ByteOrder.BIG_ENDIAN)
    .putShort(value)
    .array()


