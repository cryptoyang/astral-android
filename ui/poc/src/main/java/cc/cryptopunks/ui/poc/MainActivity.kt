package cc.cryptopunks.ui.poc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.ui.poc.api.MessengerApi
import cc.cryptopunks.ui.poc.model2.UI
import cc.cryptopunks.ui.poc.model2.eventHandler
import cc.cryptopunks.ui.poc.model2.invoke
import cc.cryptopunks.ui.poc.schema.rpc.generateOpenRpcDocument
import cc.cryptopunks.ui.poc.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCommandView()
    }

    private fun setupCommandView() {
        setContentView(R.layout.command_view)
        val doc = MessengerApi.generateOpenRpcDocument()
        val uiContext = UI.Context(doc)
        val initial = UI.State(uiContext)
        val commandView = CommandView(this)
        val handle = eventHandler(initial)
        launch {
            commandView.uiEvents3().collect { event ->
                handle(event).update(commandView).state
            }
        }
    }
}
