package cc.cryptopunks.ui.poc

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.ui.poc.api.MessengerApi
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.handleUsing
import cc.cryptopunks.ui.poc.schema.rpc.generateOpenRpcDocument
import cc.cryptopunks.ui.poc.widget.DynamicView
import cc.cryptopunks.ui.poc.widget.setupCommandView
import com.flipkart.android.proteus.value.Layout
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
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
        launch {
            findViewById<ViewGroup>(R.id.commandView)
                .setupCommandView()
                .handleUsing(uiContext)
        }
    }

    private val content by lazy { DynamicView(this) }
}

val layoutMapType = object : TypeToken<Map<String, Layout>>() {}.type

data class Screen(
    val int: Int = 4,
    val text: String = "Some text",
    val group: Group = Group(),
    val items: List<Item> = (0..10).map { Item("title$it", "Hello $it") },
)

data class Group(
    val foo: String = "Hello Foo!",
)

data class Item(
    val title: String = "",
    val text: String = "",
)
