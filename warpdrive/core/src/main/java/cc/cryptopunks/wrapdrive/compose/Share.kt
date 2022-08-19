package cc.cryptopunks.wrapdrive.compose

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.cryptopunks.astral.ui.contacts.ContactsModel
import cc.cryptopunks.astral.ui.contacts.ContactsView
import cc.cryptopunks.astral.ui.contacts.rememberContactsPreviewModel
import cc.cryptopunks.wrapdrive.R
import cc.cryptopunks.wrapdrive.model.ShareModel
import cc.cryptopunks.wrapdrive.model.isSharing
import cc.cryptopunks.wrapdrive.model.setUri
import cc.cryptopunks.wrapdrive.model.share
import cc.cryptopunks.wrapdrive.model.sharingStatus


@Preview
@Composable
fun SharePreview() = PreviewBox {
    ShareView(
        shareModel = remember {
            ShareModel().apply {
                setUri(Uri.parse("warpdrive://share/preview"))
            }
        },
        contactsModel = rememberContactsPreviewModel()
    )
}

@Composable
fun ShareView(
    shareModel: ShareModel = viewModel(),
    contactsModel: ContactsModel = viewModel(),
    selectUri: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Warp Share") },
                actions = {
                    val progress by isSharing.collectAsState()
                    if (progress) {
                        CircularProgressIndicator(
                            color = LocalContentColor.current.copy(LocalContentAlpha.current),
                            modifier = Modifier
                                .size(24.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.width(14.dp))
                    }
                    ShareButton(selectUri)
                }
            )
        },
    ) {
        Box(
            contentAlignment = Alignment.BottomStart,
        ) {
            WarpdriveConnectionView {
                ContactsView(
                    model = contactsModel
                )
            }
            SnackbarHost(snackbarHostState)
        }
    }
    val context = LocalContext.current
    val astralPackage = stringResource(id = R.string.astral_package)
    LaunchedEffect(Unit) {
        shareModel.uri.collect { (uri) ->
            when (uri) {
                Uri.EMPTY -> snackbarHostState.showSnackbar(
                    message = "Cannot obtain file uri, please select file once again",
                    duration = SnackbarDuration.Indefinite,
                )
                else -> context.run {
                    grantUriPermission(
                        astralPackage, uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        contactsModel.selected.collect { contact ->
            val peerId = contact.id
            val (uri) = shareModel.uri.value
            if (uri != Uri.EMPTY)
                share(peerId, uri)
        }
    }
    LaunchedEffect(Unit) {
        sharingStatus.collect { result ->
            var message = ""
            result.onSuccess { (_, code) ->
                message = when (code.toInt()) {
                    1 -> "Share accepted, the files are sending in background"
                    else -> "Share delivered and waiting for approval"
                }
            }
            result.onFailure {
                message = "Cannot share files: " + it.localizedMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }
}
