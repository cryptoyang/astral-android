package cc.cryptopunks.astral.wrapdrive.offer

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.astral.wrapdrive.api.EmptyInfo
import cc.cryptopunks.astral.wrapdrive.api.Info
import cc.cryptopunks.astral.wrapdrive.databinding.FileItemBinding
import cc.cryptopunks.astral.wrapdrive.util.formatSize
import kotlin.properties.Delegates

@SuppressLint("NotifyDataSetChanged")
class FileAdapter : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: FileItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        var item by Delegates.observable(EmptyInfo) { _, _, new ->
            binding.apply {
                uri.text = new.uri
                mime.text = new.mime
                progress.text = new.progress.formatSize()
                size.text = new.size.formatSize()
            }
        }
    }

    var items: List<Info> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
    }
}
