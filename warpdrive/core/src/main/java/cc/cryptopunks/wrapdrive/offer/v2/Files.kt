package cc.cryptopunks.wrapdrive.offer.v2

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.cryptopunks.astral.contacts.HorizontalDivider
import cc.cryptopunks.wrapdrive.api.Info
import cc.cryptopunks.wrapdrive.api.Offer
import cc.cryptopunks.wrapdrive.util.formatSize

@Preview
@Composable
fun FileItemsPreview() = PreviewBox {
    FileItemsView(
        offer = PreviewModel.offer,
        Modifier.fillMaxSize()
    )
}

@Composable
fun FileItemsView(
    offer: Offer,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(offer.files) { index, item: Info ->
            HorizontalDivider {
                FileItem(
                    uri = Uri.decode(item.uri),
                    size = item.size,
                    progress = when {
                        index > offer.index -> 0
                        index < offer.index -> item.size
                        else -> offer.progress
                    },
                )
            }
        }
    }
}

@Composable
private fun FileItem(
    uri: String,
    progress: Long,
    size: Long,
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = uri,
            style = MaterialTheme.typography.subtitle2
        )
        Text(
            text = "${progress.formatSize()}/${size.formatSize()}",
            style = MaterialTheme.typography.caption
        )
    }
}
