package cc.cryptopunks.ui.android

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.databind.JsonNode
import splitties.views.inflate

@SuppressLint("NotifyDataSetChanged")
class DataItemAdapter(
    var updateView: UpdateView = {},
    var onClickListener: View.OnClickListener = View.OnClickListener {},
) : RecyclerView.Adapter<DataItemAdapter.ViewHolder>() {

    var items: List<JsonNode> = emptyList()

    fun set(iterable: Iterable<JsonNode>) {
        items = iterable.toList()
        notifyDataSetChanged()
    }

    fun add(iterable: Iterable<JsonNode>) {
        val positionStart = items.size
        val newItems = iterable.toList()
        items = items + newItems
        notifyItemRangeInserted(positionStart, newItems.size)
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
