package cc.cryptopunks.ui

import cc.cryptopunks.astral.client.astralTcpNetwork
import cc.cryptopunks.astral.coder.GsonDecoder
import cc.cryptopunks.astral.coder.GsonEncoder
import cc.cryptopunks.ui.android.UIApplication
import cc.cryptopunks.ui.service.astral.AstralUIService

class App : UIApplication() {
    override val service by lazy {
        AstralUIService(
            astralTcpNetwork(
                encode = GsonEncoder(),
                decode = GsonDecoder(),
            )
        )
    }
}
