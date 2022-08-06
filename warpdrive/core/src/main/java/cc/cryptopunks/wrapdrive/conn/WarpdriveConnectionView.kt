package cc.cryptopunks.wrapdrive.conn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.cryptopunks.astral.ext.startAstralActivity
import cc.cryptopunks.astral.ext.startAstralService
import cc.cryptopunks.wrapdrive.offer.v2.PreviewBox
import cc.cryptopunks.wrapdrive.app

@Composable
fun WarpdriveConnectionView(
    content: @Composable () -> Unit,
) {
    val isConnected by if (LocalInspectionMode.current)
        remember { mutableStateOf(true) } else
        app.isConnected.collectAsState()
    if (isConnected) content()
    else DisconnectionView()
}

@Preview
@Composable
private fun DisconnectionPreview() = PreviewBox {
    DisconnectionView()
}

@Composable
private fun DisconnectionView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Cannot connect to astral network",
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(48.dp))
        Text(
            text = "Make sure the local astral service is running",
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        val context = LocalContext.current
        Button(
            onClick = { context.startAstralService() }
        ) {
            Text(text = "start astral service".uppercase())
        }
        Text(text = "or")
        Button(
            onClick = { context.startAstralActivity() }
        ) {
            Text(text = "start astral activity".uppercase())
        }
    }
}
