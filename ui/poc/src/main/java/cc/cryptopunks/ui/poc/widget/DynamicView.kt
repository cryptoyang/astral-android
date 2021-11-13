package cc.cryptopunks.ui.poc.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import cc.cryptopunks.ui.poc.app
import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.model.UI
import com.flipkart.android.proteus.*
import com.flipkart.android.proteus.value.DrawableValue
import com.flipkart.android.proteus.value.Layout
import com.flipkart.android.proteus.value.ObjectValue
import com.flipkart.android.proteus.value.Value
import com.google.gson.reflect.TypeToken

class DynamicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var onEvent: (UI.Event) -> Unit = {}

    data class Update(
        val layout: Layout? = null,
        val layouts: Map<String, Layout> = emptyMap(),
        val data: ObjectValue? = null,
        val strings: List<String> = emptyList(),
    )

    operator fun invoke(
        update: Update
    ) = update(
        layout = update.layout,
        layouts = update.layouts,
        data = update.data
    )

    fun update(
        layout: Layout? = null,
        layouts: Map<String, Layout> = emptyMap(),
        data: ObjectValue? = null,
    ) {
        if (layouts.isNotEmpty()) this.layouts.apply {
            clear()
            plusAssign(layouts)
        }
        when {
            layout != null -> {
                removeAllViews()
                proteusView = proteusInflater.inflate(layout, data ?: ObjectValue())
                addView(proteusView.asView)
            }
            data != null ->
                proteusView.viewManager.update(data)
        }
    }

    private lateinit var proteusView: ProteusView

    private val proteusInflater by lazy { proteusContext.inflater }

    private val proteusContext by lazy {
        app.proteus.createContextBuilder(context)
            .setLayoutManager(layoutManager)
            .setStyleManager(styleManager)
            .setImageLoader(imageLoader)
            .setCallback(proteusCallback)
            .build()
    }

    private var layouts: MutableMap<String, Layout> = mutableMapOf()

    private val layoutManager = object : LayoutManager() {
        override fun getLayouts() = this@DynamicView.layouts
    }

    private val styleManager = object : StyleManager() {
        private val styles: Styles = Styles()
        override fun getStyles() = styles
    }

    private val proteusCallback = object : ProteusLayoutInflater.Callback {

        override fun onUnknownViewType(
            context: ProteusContext,
            type: String,
            layout: Layout,
            data: ObjectValue,
            index: Int,
        ): ProteusView {
            TODO("Not yet implemented")
        }

        override fun onEvent(event: String, value: Value, view: ProteusView) {
            println("$event: $value")
            val id = view.viewManager.layout.extras!!.getAsString("layout")!!
            val json = app.proteusTypeAdapterFactory.COMPILED_VALUE_TYPE_ADAPTER.toJson(value)
            val data = Jackson.slimMapper.readTree(json)
            onEvent(UI.Event.Clicked(id, data))
        }
    }


    private val imageLoader = ProteusLayoutInflater
        .ImageLoader { view, url, callback: DrawableValue.AsyncCallback ->

        }
}

fun UI.State.viewUpdate(): DynamicView.Update {
    val view = stack.last()
    val dataId = view.source.result.id

    val result: Any =
        if (view.data !is List<*>) view.data
        else IterableWrapper(view.data as List<Any>)

    val main = context.layouts[dataId]!!
//        .minus("data")

    val includes = (context.layouts - dataId)
//        .filterKeys { it.startsWith("cc.cryptopunks.ui.poc.data.MessengerApi\$Contact") }

    val mapper = Jackson.prettyWriter

    val stringLayout = mapper.writeValueAsString(main)
    val stringLayouts = mapper.writeValueAsString(includes)
    val stringData = mapper.writeValueAsString(result)

    println()
    println(stringLayout)
    println(stringData)
    println()


    return DynamicView.Update(
        layout = app.gson.fromJson(stringLayout, Layout::class.java),
        layouts = app.gson.fromJson(stringLayouts, layoutMapType),
        data = app.gson.fromJson(stringData, ObjectValue::class.java),
        listOf(stringLayout, stringData, stringLayouts)
    )
}

private class IterableWrapper(
    val items: List<Any>
)

private val layoutMapType = object : TypeToken<Map<String, Layout>>() {}.type
