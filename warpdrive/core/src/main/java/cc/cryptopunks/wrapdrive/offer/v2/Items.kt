package cc.cryptopunks.wrapdrive.offer.v2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cc.cryptopunks.astral.contacts.HorizontalDivider
import cc.cryptopunks.wrapdrive.api.FilterIn
import cc.cryptopunks.wrapdrive.api.PeerOffer
import cc.cryptopunks.wrapdrive.offer.OfferModel
import cc.cryptopunks.wrapdrive.offer.formattedAmount
import cc.cryptopunks.wrapdrive.offer.formattedDateTime
import cc.cryptopunks.wrapdrive.offer.formattedInfo
import cc.cryptopunks.wrapdrive.offer.formattedName
import cc.cryptopunks.wrapdrive.offer.formattedSize
import cc.cryptopunks.wrapdrive.offer.peersOffers
import cc.cryptopunks.wrapdrive.offer.setCurrent
import cc.cryptopunks.wrapdrive.offer.shortOfferId

@Preview
@Composable
fun OfferItemsPreview() = PreviewBox {
    OfferItems(PreviewModel.instance, FilterIn)
}

@Composable
fun OfferItems(
    model: OfferModel,
    filter: String,
    navController: NavController = rememberNavController(),
) {
    val update by model.updates.getValue(filter).collectAsState()
    OfferItems(
        offers = update.peersOffers,
        filter = filter,
        details = { peerOffer ->
            model.setCurrent(peerOffer)
            navController.navigate("details")
        },
    )
}

@Composable
fun OfferItems(
    offers: List<PeerOffer>,
    filter: String,
    details: (PeerOffer) -> Unit = {},
) {
    if (offers.isEmpty()) NoOffersView(filter)
    else LazyColumn(Modifier.fillMaxSize()) {
        items(offers) { item ->
            HorizontalDivider {
                OfferItem(item) {
                    details(item)
                }
            }
        }
    }
}

@Composable
fun OfferItem(
    item: PeerOffer,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row {
            Text(
                text = shortOfferId(item.offer.id),
                style = MaterialTheme.typography.subtitle2
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = item.formattedName,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            text = item.offer.formattedInfo,
            style = MaterialTheme.typography.subtitle1,
        )
        Spacer(Modifier.height(6.dp))
        Row {
            Text(
                text = item.offer.formattedSize,
                style = MaterialTheme.typography.body2,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.offer.formattedAmount,
                style = MaterialTheme.typography.body2,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = item.offer.formattedDateTime,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}
