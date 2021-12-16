package cc.cryptopunks.ui.android

import android.app.Application
import cc.cryptopunks.ui.model.Service
import cc.cryptopunks.ui.model.UI
import cc.cryptopunks.ui.model.Handler
import cc.cryptopunks.ui.model.invoke
import cc.cryptopunks.ui.model.createContext
import cc.cryptopunks.ui.service.stub.StubServiceInterface
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class App : Application() {
    val model by lazy {
        Handler(UI.State(service))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        schemaUpdates
    }

    private val scope = MainScope()

    private val service: Service.Interface by lazy {
        StubServiceInterface()
    }

    private val schemaUpdates by lazy {
        scope.launch {
            service.schemas()
                .map(Service.Schema::createContext)
                .map(UI.Action::AddContext)
                .collect(model::handle)
        }
    }

}

val app get() = instance

private lateinit var instance: App
