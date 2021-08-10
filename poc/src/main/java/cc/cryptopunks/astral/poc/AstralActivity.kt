package cc.cryptopunks.astral.poc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AstralActivity : AppCompatActivity() {

    private val serviceConnection = AstralServiceConnection { service() }
    private val clientConnection = AstralServiceConnection { client() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(AstralService.intent(this))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        println("Bind services")
        serviceConnection bind this
        clientConnection bind this
    }

    override fun onDestroy() {
        serviceConnection unbind this
        clientConnection unbind this
        super.onDestroy()
    }
}
