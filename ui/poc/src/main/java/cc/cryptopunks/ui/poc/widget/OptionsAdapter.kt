package cc.cryptopunks.ui.poc.widget

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import cc.cryptopunks.ui.poc.databinding.CommandItemBinding
import cc.cryptopunks.ui.poc.databinding.TextItemBinding
import cc.cryptopunks.ui.poc.model.Api
import cc.cryptopunks.ui.poc.model.UIMatching
import cc.cryptopunks.ui.poc.model.UIMethodScore
import kotlin.properties.Delegates

class ViewBindingHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)


@SuppressLint("NotifyDataSetChanged")
class OptionsAdapter(
    var onClickListener: View.OnClickListener = View.OnClickListener { },
) : RecyclerView.Adapter<ViewBindingHolder>() {

    var items: List<Any> by Delegates.observable(emptyList()) { property, oldValue, newValue ->
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = when (viewType) {
            0 -> TextItemBinding.inflate(inflater, parent, false)
            1 -> CommandItemBinding.inflate(inflater, parent, false)
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
        binding.root.setOnClickListener(onClickListener)
        return ViewBindingHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewBindingHolder, position: Int) {
        val item = items[position]
        when (item) {
            is String -> holder.itemView.let { it as TextView }.text = item
            is Boolean -> holder.itemView.let { it as TextView }.text = item.toString()
            is UIMethodScore -> holder.binding.let { it as CommandItemBinding }.set(item)
            is UIMatching -> holder.binding.let { it as CommandItemBinding }.set(item)
            is Api.Method -> holder.binding.let { it as CommandItemBinding }.set(item)
        }
        holder.itemView.tag = item
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is String -> 0
        is Boolean -> 0
        is UIMethodScore -> 1
        is UIMatching -> 1
        is Api.Method -> 1
        else -> -1
    }
}

