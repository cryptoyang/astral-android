package cc.cryptopunks.ui.poc.widget

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.ui.poc.R
import cc.cryptopunks.ui.poc.mapper.Jackson
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UILayout
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import splitties.views.inflateAndAttach

class DataView(
    val root: ViewGroup,
    val header: FrameLayout = root.findViewById(R.id.header),
    val content: RecyclerView = root.findViewById(R.id.content),
    val footer: FrameLayout = root.findViewById(R.id.footer),
) {
    var updateView: UpdateView = displayYaml

    var layout: UILayout = UILayout.Empty

    var onEvent: (UI.Event) -> Unit = {}

    val dataAdapter = DataAdapter()

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

fun DataView.update(
    layout: UILayout,
    data: JsonNode,
    updateView: UpdateView = displayYaml
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
            dataAdapter.items = when {
                node.isArray -> (node as ArrayNode).toList()
                node.isEmpty -> emptyList()
                else -> listOf(node)
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

fun JsonNode.resolve(path: List<String>) =
    path.fold(this) { acc, s -> acc[s] ?: Jackson.emptyNode }
