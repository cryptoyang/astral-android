package cc.cryptopunks.wrapdrive.offer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri

fun Context.hasWriteStoragePermissions(): Boolean {
    var granted = false
    packageManager
        .getInstalledPackages(PackageManager.GET_PERMISSIONS)
        .find { it.packageName == "cc.cryptopunks.astral" }?.run {
            val index = requestedPermissions.indexOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (index > -1) {
                val flags = requestedPermissionsFlags[index]
                granted = flags and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0
            }
        }
    return granted
}

fun Context.startWritePermissionActivity() {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("astral://permissions/write/storage"))
    startActivity(intent)
}
