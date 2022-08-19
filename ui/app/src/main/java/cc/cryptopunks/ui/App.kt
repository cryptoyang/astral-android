package cc.cryptopunks.ui

import cc.cryptopunks.astral.client.enc.encoder
import cc.cryptopunks.astral.client.gson.GsonCoder
import cc.cryptopunks.astral.client.tcp.astralTcpNetwork
import cc.cryptopunks.ui.android.UIApplication
import cc.cryptopunks.ui.service.astral.AstralUIService

class App : UIApplication() {
    override val service by lazy {
        AstralUIService(astralTcpNetwork().encoder(GsonCoder()))
    }
}
