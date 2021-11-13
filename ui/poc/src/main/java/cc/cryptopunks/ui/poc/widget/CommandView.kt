package cc.cryptopunks.ui.poc.widget

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.ui.poc.R
import cc.cryptopunks.ui.poc.databinding.CommandItemBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import splitties.views.imageDrawable
import kotlin.properties.Delegates


data class CommandView(
    val activity: AppCompatActivity,
    val container: ViewGroup = activity.findViewById(R.id.commandView),
    val toolbar: Toolbar = container(R.id.toolbar),
    val dynamicView: DynamicView = container(R.id.dynamicView),
    val commandLayout: ViewGroup = container(R.id.selectedCommandContainer),
    val commandView: ViewGroup = container(R.id.selectedCommandLayout),
    val recyclerView: RecyclerView = container(R.id.optionsRecyclerView),
    val inputView: EditText = container(R.id.inputEditText),
    val actionButton: View = container(R.id.actionButton),
) {
    val cmdDrawable: Drawable = ShapeTextDrawable("$")

    val returnDrawable: Drawable = ResourcesCompat.getDrawable(
        container.resources,
        R.drawable.baseline_keyboard_return_white_18dp,
        container.context.theme
    )!!

    val optionsAdapter = OptionsAdapter()

    val commandBinding = CommandItemBinding.bind(commandView)

    var showInterface by Delegates.observable(true) { _, shown, show ->
        if (shown != show) {
            dynamicView.isVisible = !show
            recyclerView.isVisible = show
            inputView.isVisible = show
            commandLayout.isVisible = show && commandLayout.tag != null
        }
    }

    init {

        activity.setSupportActionBar(toolbar)

        recyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
//            stackFromEnd = true
            }
            val dividerItemDecoration = DividerItemDecoration(
                recyclerView.context,
                linearLayoutManager.orientation
            )

            layoutManager = linearLayoutManager
            adapter = optionsAdapter
            addItemDecoration(dividerItemDecoration)
        }

    }
}

operator fun <T : View> View.invoke(@IdRes id: Int): T =
    findViewById(id)

fun ComponentActivity.backEventsFlow(): Flow<Unit> = callbackFlow {
    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            trySend(Unit)
        }
    }
    onBackPressedDispatcher.addCallback(onBackPressedCallback)
    awaitClose {
        onBackPressedCallback.remove()
    }
}

fun EditText.textChangesFlow(): Flow<String?> = callbackFlow {
    val watcher = doOnTextChanged { text, _, _, _ ->
        trySend(text?.toString())
    }
    awaitClose {
        removeTextChangedListener(watcher)
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
