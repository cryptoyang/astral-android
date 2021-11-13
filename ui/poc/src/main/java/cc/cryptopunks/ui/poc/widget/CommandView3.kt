package cc.cryptopunks.ui.poc.widget

import android.view.View
import androidx.core.view.isVisible
import cc.cryptopunks.ui.poc.model.Score
import cc.cryptopunks.ui.poc.model2.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun CommandView.uiEvents3(): Flow<UI.Event> = channelFlow {

    operator fun UI.Event.unaryPlus() = trySend(this)

    actionButton.setOnClickListener {
        +UI.Event.Action
    }

    optionsAdapter.onClickListener = View.OnClickListener { view ->
        when (val item = view.tag) {
            is Score -> +UI.Event.Method(item.method)
            is String -> +UI.Event.Clicked("string", item)
            is Boolean -> +UI.Event.Clicked("boolean", item)
        }
    }

    dynamicView.onEvent = { event ->
        +event
    }

    launch {
        inputView.textChangesFlow().collect { text ->
            +UI.Event.Text(text)
        }
    }

    launch {
        activity.backEventsFlow().collect {
            +UI.Event.Back
        }
    }

    launch { +UI.Event.Init }
}

fun UI.Change.update(view: CommandView): UI.Change =
    apply {
        output.forEach { output ->
            view.update(state, output)
        }
    }

fun CommandView.update(state: UI.State, output: UI.Output) {
    when (output) {

        UI.Element.Text -> {
            if (state.text != inputView.text.toString())
                inputView.setText(state.text)
        }

        UI.Element.Methods -> {
            inputView.hint = "find command by name or param"
            optionsAdapter.items = state.methods
        }

        UI.Element.Param -> state.param?.run {
            when (
                val resolver = resolvers.minOrNull()
            ) {
                is UIResolver.Data -> {
                    dynamicView.isVisible = true
                    recyclerView.isVisible = false
                }
                is UIResolver.Option -> {
                    optionsAdapter.items = resolver.list
                    dynamicView.isVisible = false
                    recyclerView.isVisible = true
                }
                is UIResolver.Input -> when (resolver.type) {
                    "boolean" -> {
                        optionsAdapter.items = listOf(false, true)
                        dynamicView.isVisible = false
                        recyclerView.isVisible = true
                    }
                    else -> {
                        dynamicView.isVisible = false
                        recyclerView.isVisible = false
                    }
                }
                is UIResolver.Method -> {
                    dynamicView.isVisible = false
                    recyclerView.isVisible = true
                }
            }

            inputView.hint = "provide $name"
        }

        UI.Element.Method, is UI.Element.Args -> {
            commandLayout.isVisible = state.method != null
            state.method?.let { method ->
                commandBinding.set(
                    method,
                    state.args.mapValues { (_, value) -> value.toString() }
                )
            }
        }

        UI.Element.Display -> state.display.let { element ->
            val displayPanel = element == UIDisplay.Panel
            val displayCommand = displayPanel && UI.Element.Method in state
            val displayData = element == UIDisplay.Data
            inputView.isVisible = displayPanel
            recyclerView.isVisible = displayPanel
            commandLayout.isVisible = displayCommand
            dynamicView.isVisible = displayData
        }

        UI.Element.Ready -> {
            inputView.hint = "Tap FAB to execute."
        }

        is UI.Element.Stack -> {
            if (state.stack.isNotEmpty()) {
                dynamicView(state.viewUpdate())
            }
            activity.supportActionBar!!.title = state.stack.lastOrNull()?.source?.id
        }

        UI.Action.Exit -> {

            activity.finish()
        }
    }
}
