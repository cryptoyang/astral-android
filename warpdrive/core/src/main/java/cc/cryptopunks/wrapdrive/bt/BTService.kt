package cc.cryptopunks.wrapdrive.bt

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow

fun pairedDevices() = flow {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    val devices = adapter?.bondedDevices
    devices?.forEach { device ->
        emit(device)
    }
}

fun startBluetoothDiscovery() {
    BluetoothAdapter.getDefaultAdapter()?.startDiscovery()
}

fun Context.discoveredBluetoothDevices(): Flow<BluetoothDevice> = channelFlow {

    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            trySend(device)
        }
    }

    registerReceiver(receiver, filter)

    awaitClose {
        unregisterReceiver(receiver)
    }
}

fun Activity.makeBluetoothDiscoverable() {

    val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
    }
    startActivityForResult(discoverableIntent, bluetoothRequestCode)
}

fun isBluetoothDiscoverable(requestCode: Int, resultCode: Int): Boolean =
    requestCode == bluetoothRequestCode && resultCode != Activity.RESULT_CANCELED


private const val bluetoothRequestCode = 100
