package cc.cryptopunks.wrapdrive.offer.v2

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cc.cryptopunks.wrapdrive.R
import cc.cryptopunks.wrapdrive.offer.OfferModel
import cc.cryptopunks.wrapdrive.theme.AppTheme
import cc.cryptopunks.wrapdrive.conn.WarpdriveConnectionView
import cc.cryptopunks.wrapdrive.util.shareIntent

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
                    ShareButton()
                }
            )
        },
    ) {
        WarpdriveConnectionView {
            val navController = rememberNavController()
            NavHost(navController, "offers") {
                composable("offers") {
                    DashboardView(model, navController)
                }
                composable("details") {
                    OfferDetailsView(model)
                }
            }
            val currentId by model.currentId.collectAsState()
            if (currentId != null) LaunchedEffect(Unit) {
                navController.navigate("details")
            }
        }
    }
}

@Composable
fun ShareButton() {
    val context = LocalContext.current
    ShareButton { context.run { startActivity(shareIntent()) } }
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
