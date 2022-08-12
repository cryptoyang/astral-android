package cc.cryptopunks.wrapdrive.compose

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import cc.cryptopunks.wrapdrive.R
import cc.cryptopunks.wrapdrive.model.OfferModel
import cc.cryptopunks.wrapdrive.model.subscribeChanges
import cc.cryptopunks.wrapdrive.startShareActivity

@Preview
@Composable
fun MainPreview() {
    MainView(PreviewModel.instance)
}

@Composable
fun MainView(
    model: OfferModel,
) = AppTheme {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Warp Drive")
                },
                actions = {
                    val context = LocalContext.current
                    ShareButton { context.startShareActivity() }
                }
            )
        },
    ) {
        WarpdriveConnectionView {
            DashboardView(model)
            val currentId by model.currentId.collectAsState()
            if (currentId != null) OfferDetailsView(model)
            LaunchedEffect(Unit) { model.subscribeChanges() }
        }
    }
}

@Composable
fun ShareButton(
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_share_white_24dp),
            contentDescription = "share"
        )
    }
}
