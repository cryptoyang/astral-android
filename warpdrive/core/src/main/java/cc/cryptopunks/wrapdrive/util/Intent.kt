package cc.cryptopunks.wrapdrive.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import cc.cryptopunks.wrapdrive.api.OfferId
import cc.cryptopunks.wrapdrive.share.v2.ShareActivity

fun astralIntent() = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("astral://main")
)

fun Context.shareIntent() = Intent(this, ShareActivity::class.java)

fun offerIntent(offerId: OfferId) = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("warpdrive://offer/$offerId")
)

fun ActivityResultLauncher<String>.startFileChooser() = launch("*/*")
