package cc.cryptopunks.ui.poc.widget

import android.annotation.SuppressLint
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
import splitties.views.inflate
import splitties.views.inflateAndAttach
import kotlin.properties.Delegates

class DataView(
    val root: ViewGroup,
    val header: FrameLayout = root.findViewById(R.id.header),
    val content: RecyclerView = root.findViewById(R.id.content),
    val footer: FrameLayout = root.findViewById(R.id.footer),
) {
    var updateView: UpdateView = displayJson

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
    (get(0) as TextView).text = Jackson.prettyWriter.writeValueAsString(data)
}

typealias UpdateView = ViewGroup.(JsonNode) -> Unit

fun DataView.update(layout: UILayout, data: JsonNode) {
    this.layout = layout
    header.apply {
        if (layout.header != UILayout.Single.Empty) {
            val node = layout.header.path.fold(data) { acc, s -> acc[s] }
            updateView(root, node)
            isVisible = true
        } else {
            isVisible = false
        }
    }
    content.run {
        if (layout.content != UILayout.Many.Empty) {
            val node = layout.content.path.fold(data) { acc, s -> acc[s] }
            dataAdapter.items = when {
                node.isArray -> (node as ArrayNode).toList()
                else -> listOf(node)
            }
            isVisible = true
        } else {
            isVisible = false
        }
    }
    footer.apply {
        if (layout.footer != UILayout.Single.Empty) {
            val node = layout.footer.path.fold(data) { acc, s -> acc[s] }
            updateView(root, node)
            isVisible = true
        } else {
            isVisible = false
        }
    }
}


@SuppressLint("NotifyDataSetChanged")
class DataAdapter(
    var updateView: UpdateView = {},
    var onClickListener: View.OnClickListener = View.OnClickListener { },
) : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    var items: List<JsonNode> by Delegates.observable(emptyList()) { property, oldValue, newValue ->
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.data_item_view, false))
            .apply { itemView.setOnClickListener(onClickListener) }

    class ViewHolder(val view: ViewGroup) : RecyclerView.ViewHolder(view)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateView(holder.view, items[position])
    }

    override fun getItemCount(): Int = items.size
}
