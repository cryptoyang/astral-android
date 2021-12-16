package cc.cryptopunks.ui.android

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.interpret
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val uiEvents = Channel<UI.Event>(Channel.BUFFERED)

    private val commandView: CommandBinding by lazy {
        setContentView(R.layout.main)
        CommandBinding(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commandView
        launch {
            merge(
                uiEvents.consumeAsFlow(),
                commandView.uiEvents(),
            ).collect { action ->
                app.model.handle(action)
            }
        }
        launch {
            app.model.changes.collect { change ->
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
