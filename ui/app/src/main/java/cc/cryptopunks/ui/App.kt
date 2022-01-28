package cc.cryptopunks.ui

import cc.cryptopunks.astral.gson.GsonCoder
import cc.cryptopunks.astral.tcp.astralTcpNetwork
import cc.cryptopunks.ui.android.UIApplication
import cc.cryptopunks.ui.service.astral.AstralUIService

class App : UIApplication() {
    override val service by lazy {
        AstralUIService(astralTcpNetwork(GsonCoder()))
    }
}
