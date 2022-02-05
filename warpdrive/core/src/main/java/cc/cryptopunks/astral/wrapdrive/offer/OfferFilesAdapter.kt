package cc.cryptopunks.astral.wrapdrive.offer

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.cryptopunks.astral.wrapdrive.api.EmptyInfo
import cc.cryptopunks.astral.wrapdrive.api.Info
import cc.cryptopunks.astral.wrapdrive.databinding.FileItemBinding
import cc.cryptopunks.astral.wrapdrive.util.formatSize
import kotlin.properties.Delegates

class OfferFilesAdapter : RecyclerView.Adapter<OfferFilesAdapter.ViewHolder>() {

    var index: Int = -1
    var progress: Long = 0
    var items: List<Info> = emptyList()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = items[position]
        holder.progress = when {
            position > index -> 0
            position < index -> holder.item.size
            else -> progress
        }
    }

    @SuppressLint("SetTextI18n")
    inner class ViewHolder(
        private val binding: FileItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        var progress by Delegates.observable(0L) { _, _, new ->
            binding.progress.text =  "${new.formatSize()}/"
        }
        var item by Delegates.observable(EmptyInfo) { _, _, new ->
            binding.apply {
                uri.text = Uri.decode(new.uri)
                mime.text = new.mime
                size.text = new.size.formatSize()
            }
        }
    }
}
