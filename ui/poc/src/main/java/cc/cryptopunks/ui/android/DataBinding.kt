package cc.cryptopunks.ui.android

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.UILayout
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import kotlinx.coroutines.flow.*
import splitties.views.inflateAndAttach

class DataBinding(
    val root: ViewGroup,
    val header: FrameLayout = root.findViewById(R.id.header),
    val content: RecyclerView = root.findViewById(R.id.content),
    val footer: FrameLayout = root.findViewById(R.id.footer),
) {
    var updateView: UpdateView = displayYaml

    var layout: UILayout = UILayout.Empty

    var onEvent: (UI.Event) -> Unit = {}

    val dataAdapter = DataItemAdapter()

    var isVisible
        get() = root.isVisible
        set(value) {
            root.isVisible = value
        }

    init {
        dataAdapter.updateView = updateView
        dataAdapter.onClickListener = View.OnClickListener { view ->
            val id = layout.content.elements.type.id
            val node = view.tag as JsonNode
            onEvent(UI.Event.Clicked(id, node))
        }
        content.layoutManager = LinearLayoutManager(root.context)
        content.adapter = dataAdapter
    }
}

val displayJson: UpdateView = { data ->
    removeAllViews()
    tag = data
    inflateAndAttach(R.layout.json_view)
    (get(0) as TextView).text = Jackson.jsonPrettyWriter.writeValueAsString(data)
}

val displayYaml: UpdateView = { data ->
    removeAllViews()
    tag = data
    inflateAndAttach(R.layout.json_view)
    (get(0) as TextView).text = Jackson.yamlMapper.writeValueAsString(data)
}

typealias UpdateView = ViewGroup.(JsonNode) -> Unit


suspend fun DataBinding.update(
    layouts: Flow<UILayout>,
    data: Flow<JsonNode>,
    views: Flow<UpdateView> = flowOf(displayYaml)
): Unit = combine(layouts, data, views) { layout, json, update ->
    update(layout, json, update)
}.collect()

suspend fun DataBinding.update(
    layout: UILayout,
    updates: Flow<JsonNode>,
    updateView: UpdateView = displayYaml
) = updates.collect { data ->
    update(layout, data, updateView)
}

fun DataBinding.update(
    layout: UILayout,
    data: JsonNode,
    updateView: UpdateView = displayYaml,
) {
    this.layout = layout
    header.apply {
        if (layout.header != UILayout.Single.Empty) {
            val node = layout.header.path.fold(data) { acc, s -> acc[s] }
            updateView(node)
        } else {
            removeAllViews()
        }
    }
    content.run {
        if (layout.content != UILayout.Many.Empty) {
            val node = data.resolve(layout.content.path)
            dataAdapter.updateView = updateView
            when {
                node.isEmpty -> dataAdapter.set(emptyList())
                node.isArray -> dataAdapter.set((node as ArrayNode).toList())
                node.isObject -> when {
                    node["add"] != null -> dataAdapter.add((node["add"] as ArrayNode))
                }
            }
        } else {
            dataAdapter.items = emptyList()
        }
    }
    footer.apply {
        if (layout.footer != UILayout.Single.Empty) {
            val node = layout.footer.path.fold(data) { acc, s -> acc[s] }
            updateView(node)
        } else {
            removeAllViews()
        }
    }
}

private fun JsonNode.resolve(path: List<String>) =
    path.fold(this) { acc, s -> acc[s] ?: Jackson.emptyNode }
