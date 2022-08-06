package cc.cryptopunks.wrapdrive.offer.v2

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.cryptopunks.wrapdrive.api.PeerOffer
import cc.cryptopunks.wrapdrive.offer.OfferModel
import cc.cryptopunks.wrapdrive.offer.download
import cc.cryptopunks.wrapdrive.offer.formattedName
import cc.cryptopunks.wrapdrive.offer.formattedStatus
import cc.cryptopunks.wrapdrive.offer.shortOfferId
import cc.cryptopunks.wrapdrive.offer.startWritePermissionActivity
import java.text.DateFormat
import java.text.SimpleDateFormat

@Preview
@Composable
fun OfferDetailsPreview() = PreviewBox {
    OfferDetailsView(PreviewModel.instance)
}

@Composable
fun OfferDetailsView(
    model: OfferModel = viewModel(),
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .fillMaxSize(),
    ) {
        val data by model.current.collectAsState()
        OfferHeaderView(data)
        FileItemsView(
            offer = data.offer,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        val context = LocalContext.current
        if (data.offer.isIncoming) DownloadButton {
            if (model.hasWritePermission) model.download()
            else context.startWritePermissionActivity()
        }
    }
}

@Composable
private fun OfferHeaderView(
    data: PeerOffer,
) {
    val (peer, offer) = data
    OfferHeaderView(
        offerId = shortOfferId(offer.id),
        contactName = PeerOffer(peer, offer).formattedName,
        status = offer.formattedStatus,
        createdAt = offer.create,
        updateAt = offer.update,
    )
}

@Composable
private fun OfferHeaderView(
    offerId: String,
    contactName: String,
    status: String,
    createdAt: Long,
    updateAt: Long,
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = offerId,
                style = MaterialTheme.typography.h6
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = contactName,
                style = MaterialTheme.typography.subtitle1
            )
        }
        Spacer(Modifier.height(16.dp))
        val dateOffset = 120.dp
        Box {
            Text(
                text = "Created at",
                style = MaterialTheme.typography.subtitle2,
            )
            Spacer(Modifier.width(32.dp))
            Text(
                text = dateTime.format(createdAt),
                modifier = Modifier.offset(x = dateOffset),
            )
        }
        Box {
            Text(
                text = status,
                style = MaterialTheme.typography.subtitle2,
            )
            Text(
                text = when (createdAt) {
                    updateAt -> ""
                    else -> dateTime.format(updateAt)
                },
                modifier = Modifier.offset(x = dateOffset),
            )
        }
    }
}

@Composable
private fun DownloadButton(
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.padding(8.dp),
        onClick = onClick,
    ) {
        Text(
            text = "Download".uppercase(),
            style = MaterialTheme.typography.button
        )
    }
}

private val dateTime: DateFormat = SimpleDateFormat.getDateTimeInstance()
