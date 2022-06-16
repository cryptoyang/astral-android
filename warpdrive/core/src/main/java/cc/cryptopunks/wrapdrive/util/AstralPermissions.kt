package cc.cryptopunks.wrapdrive.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import cc.cryptopunks.wrapdrive.R


val Context.hasBluetoothPermissions: Boolean
    get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

val Context.hasWriteStoragePermissions: Boolean
    get() = hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

fun Context.requestWriteStoragePermission() = startAstralPermissionActivity(
    grant = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    text = getString(R.string.write_permission_text),
)

fun Context.hasPermission(
    permission: String
): Boolean {
    var granted = false
    packageManager
        .getInstalledPackages(PackageManager.GET_PERMISSIONS)
        .find { it.packageName == "cc.cryptopunks.astral" }?.run {
            val index = requestedPermissions.indexOf(permission)
            if (index > -1) {
                val flags = requestedPermissionsFlags[index]
                granted = flags and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0
            }
        }
    return granted
}

fun Context.startAstralPermissionActivity(
    grant: String,
    text: String,
) {
    val uri = Uri.Builder()
        .scheme("astral")
        .authority("permissions")
        .appendQueryParameter("grant", grant)
        .appendQueryParameter("text", text)
        .build()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(intent)
}

