package cc.cryptopunks.ui.poc

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.interpret
import cc.cryptopunks.ui.poc.widget.CommandView
import cc.cryptopunks.ui.poc.widget.uiEvents
import cc.cryptopunks.ui.poc.widget.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val commandView: CommandView by lazy {
        setContentView(R.layout.command_view)
        CommandView(this)
    }

    private val uiEvents = Channel<UI.Event>(Channel.BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commandView
        setupCommandView()
    }

    private fun setupCommandView() {
        launch {
            merge(
                uiEvents.consumeAsFlow(),
                commandView.uiEvents(),
            ).mapNotNull {
                app.model.state.interpret(it)
            }.collect {
                app.model.handle(it)
            }
        }
        launch {
            app.model.changes().collect { change ->
                change.update(commandView)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = !item.isChecked

        val propertyName = when (item.itemId) {
            R.id.autoFill -> "autoFill"
            R.id.autoExecute -> "autoExecute"
            else -> throw IllegalArgumentException()
        }
        val event = UI.Event.Configure(mapOf(propertyName to item.isChecked))
        uiEvents.trySend(event)
        return true
    }
}
