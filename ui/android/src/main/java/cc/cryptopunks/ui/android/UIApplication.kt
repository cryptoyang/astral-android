package cc.cryptopunks.ui.android

import android.app.Application
import cc.cryptopunks.ui.model.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class UIApplication : Application() {

    protected abstract val service: Service.Interface

    val model by lazy {
        Handler(UI.State(service))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        schemaUpdates
    }

    private val scope = MainScope()

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

private lateinit var instance: UIApplication
