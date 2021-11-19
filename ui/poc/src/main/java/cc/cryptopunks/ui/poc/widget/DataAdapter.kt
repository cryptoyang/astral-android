package cc.cryptopunks.ui.poc.widget

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.ui.poc.R
import com.fasterxml.jackson.databind.JsonNode
import splitties.views.inflate
import kotlin.properties.Delegates

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
