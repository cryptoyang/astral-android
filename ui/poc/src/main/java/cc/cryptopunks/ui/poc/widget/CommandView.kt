package cc.cryptopunks.ui.poc.widget

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.ui.poc.*
import cc.cryptopunks.ui.poc.databinding.CommandItemBinding
import cc.cryptopunks.ui.poc.model.Command
import cc.cryptopunks.ui.poc.model.Score
import cc.cryptopunks.ui.poc.model.UI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import splitties.views.imageDrawable
import java.util.*
import kotlin.properties.Delegates

suspend fun ViewGroup.setupCommandView(
    input: Channel<Command.Output> = Channel(Channel.BUFFERED),
    dynamicView: DynamicView = findViewById(R.id.dynamicView),
    commandLayout: ViewGroup = findViewById(R.id.selectedCommandContainer),
    commandView: ViewGroup = findViewById(R.id.selectedCommandLayout),
    recyclerView: RecyclerView = findViewById(R.id.optionsRecyclerView),
    inputView: EditText = findViewById(R.id.inputEditText),
    actionButton: View = findViewById(R.id.actionButton),
): Pair<SendChannel<Command.Output>, Flow<Command.Input>> =
    input as SendChannel<Command.Output> to channelFlow {

        val cmdDrawable = ShapeTextDrawable("$")

        val returnDrawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.baseline_keyboard_return_white_18dp,
            context.theme
        )

        var status = Command.Status.Empty

        val commandBinding = CommandItemBinding.bind(commandView)

        fun Command.Input.out() = trySend(this)

        (actionButton as FloatingActionButton).foreground = ShapeTextDrawable("$")

        var showInterface by Delegates.observable(true) { _, shown, show ->
            if (shown != show) {
                recyclerView.isVisible = show
                inputView.isVisible = show
                commandLayout.isVisible = show && status != Command.Status.Empty
            }
        }
        actionButton.setOnClickListener {
//            showInterface = !showInterface
            Command.Execute.out()
        }

        inputView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) Command.InputText(inputView.text.toString()).out()
        }

        var hadText = false
        val watcher = inputView.doOnTextChanged { text, start, before, count ->
            val hasText = !text.isNullOrBlank()
            if (hadText != hasText) actionButton.setIcon(
                if (hasText) returnDrawable
                else cmdDrawable
            )
            hadText = hasText
            Command.InputText(text.toString()).out()
        }

        val adapter = OptionsAdapter { view ->
            when (val item = view.tag) {
                is Score -> Command.Selected(item.method).out()
                is String -> {
                    val param = inputView.tag as Command.ProvideParam
                    Command.SetParam(param.name, item).out()
                }
            }
        }

        recyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
//            stackFromEnd = true
            }
            val dividerItemDecoration = DividerItemDecoration(
                recyclerView.context,
                linearLayoutManager.orientation
            )
            addItemDecoration(dividerItemDecoration)
            layoutManager = linearLayoutManager
            this.adapter = adapter
        }


        fun Command.Output.handle() {
            when (this) {

                is Command.ProvideParam -> inputView.apply {
                    text.clear()
                    hint = "$name: $type"
                    tag = this@handle

                    val pathOptions = resolvers
                        .filterIsInstance<UI.Resolver.Path>()

                    val stringOptions = resolvers
                        .filterIsInstance<UI.Resolver.Option>()
                        .flatMap(UI.Resolver.Option::list)

                    val booleanOptions: List<String> = resolvers
                        .filterIsInstance<UI.Resolver.Input>()
                        .firstOrNull { it.type == "boolean" }?.let {
                            listOf("true", "false")
                        } ?: emptyList()

                    adapter.items = stringOptions + booleanOptions
                }

                is Command.Status -> {
                    status = this
                    commandBinding.set(method, args)
                    commandView.run { parent as View }.isVisible = true
                }

                is Command.SelectMethod -> {
                    adapter.items = list
                }

                Command.Ready -> {
                    inputView.hint = "Tap FAB to execute."
                    adapter.items = emptyList()
                }

                is Command.Update -> {
                    val hasData = context.data.isNotEmpty()

                    recyclerView.isVisible = !hasData
                    dynamicView.isVisible = hasData

                    if (hasData) {
                        val update = context.viewUpdate()
                        dynamicView(update)
                    }
                }
            }
        }

        launch { input.consumeAsFlow().collect { it.handle() } }

        launch { Command.GetMethods.out() }

        awaitClose {
            inputView.removeTextChangedListener(watcher)
        }
    }

private fun FloatingActionButton.setIcon(drawable: Drawable? = null) {
    when (drawable) {
        is ShapeTextDrawable -> {
            foreground = drawable
            imageDrawable = null
        }
        else -> {
            foreground = null
            imageDrawable = drawable
        }
    }
}
