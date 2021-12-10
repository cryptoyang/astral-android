package cc.cryptopunks.ui.poc

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.ui.poc.api.MessengerApi
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIUpdate
import cc.cryptopunks.ui.poc.model.eventHandler
import cc.cryptopunks.ui.poc.model.eventHandler2
import cc.cryptopunks.ui.poc.model.factory.invoke
import cc.cryptopunks.ui.poc.schema.rpc.generateOpenRpcDocument
import cc.cryptopunks.ui.poc.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val commandView: CommandView by lazy {
        setContentView(R.layout.command_view)
        CommandView(this)
    }

    private val handle by lazy {
        val doc = MessengerApi.generateOpenRpcDocument()
        val uiContext = UI.Context(doc)
        val initial = UI.State(uiContext)
        eventHandler2(initial)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commandView
        setupCommandView()
    }

    private fun setupCommandView() {
        launch {
            commandView.uiEvents().collect { event -> handleAndUpdate(event) }
        }
    }

    private fun handleAndUpdate(event: UI.Event) {
        handle(event).printLog().update(commandView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val propertyName = when (item.itemId) {
            R.id.autoFill -> "autoFill"
            R.id.autoExecute -> "autoExecute"
            else -> throw IllegalArgumentException()
        }
        item.isChecked = !item.isChecked
        val event = UI.Event.Configure(mapOf(propertyName to item.isChecked))
        handleAndUpdate(event)
        return true
    }
}

fun UI.Change.printLog() = also {
    StringBuilder().apply {
        appendLine()
        appendLine("=========")
        appendLine(event.formatLogName() + ": " + event)
        appendLine("---------")
        output.map { message ->
            val (out, value) = when (message) {
                is UI.Action -> message to message
                is UIUpdate<*, *> -> message.run { element to value }
            }
            out.formatLogName() + ": " + value
        }.forEach(this::appendLine)
        appendLine("=========")
    }.toString().let(::println)
}

fun Any.formatLogName(): String = javaClass.name.split("$", limit = 2).last()
