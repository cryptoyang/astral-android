package cc.cryptopunks.wrapdrive.offer.v2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.cryptopunks.wrapdrive.R
import cc.cryptopunks.wrapdrive.api.FilterIn
import cc.cryptopunks.wrapdrive.api.FilterOut
import cc.cryptopunks.wrapdrive.util.startShareActivity

@Composable
fun NoOffersView(
    filter: String,
) {
    when (filter) {
        FilterIn -> NoReceivedOffers()
        FilterOut -> NoSentOffers()
    }
}

@Preview
@Composable
fun NoReceivedOffersPreview() = PreviewBox {
    NoReceivedOffers()
}

@Composable
fun NoReceivedOffers() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Here, you will find your received files offers",
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp),
        )
        Text(
            text = "...Just after the first one arrive",
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Preview
@Composable
fun NoSentOffersPreview() = PreviewBox {
    NoSentOffers()
}

@Composable
fun NoSentOffers() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "To send a file",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 64.dp),
            style = MaterialTheme.typography.h5
        )
        Text(
            text = """
                1. Find a file and tap share button

                3. Select Warp Share

                4. Choose a contact to send files offer
            """.trimIndent(),
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        Text(
            text = "You can choose app that allows to share a file by yourself or click the button down below",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        val context = LocalContext.current
        Button(
            onClick = { context.startShareActivity() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_share_white_24dp),
                contentDescription = "share file"
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "share file".uppercase()
            )
        }
    }
}
