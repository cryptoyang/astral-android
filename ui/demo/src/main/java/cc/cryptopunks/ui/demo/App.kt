package cc.cryptopunks.ui.demo

import cc.cryptopunks.ui.android.UIApplication
import cc.cryptopunks.ui.service.stub.StubServiceInterface

class App : UIApplication() {
    override val service by lazy(::StubServiceInterface)
}

