package cc.cryptopunks.wrapdrive.api.client

import android.bluetooth.BluetoothAdapter
import cc.cryptopunks.astral.enc.EncNetwork
import cc.cryptopunks.astral.ext.byte
import cc.cryptopunks.astral.ext.decodeList
import cc.cryptopunks.astral.ext.queryResult
import cc.cryptopunks.astral.service.bluetooth.discoverServices
import cc.cryptopunks.wrapdrive.api.Peer
import cc.cryptopunks.wrapdrive.api.QueryPeers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

suspend fun EncNetwork.peers(): List<Peer> =
    queryResult(QueryPeers) {
        result = decodeList()
        byte = 0
    }

fun bluetoothPeers(): Flow<Peer> =
    BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.discoverServices().map {
        Peer(
            id = it.address,
            alias = it.name ?: "",
            network = "bt"
        )
    }
