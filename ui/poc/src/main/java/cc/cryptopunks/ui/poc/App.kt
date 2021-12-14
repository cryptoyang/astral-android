package cc.cryptopunks.ui.poc

import android.app.Application
import cc.cryptopunks.ui.poc.model.Service
import cc.cryptopunks.ui.poc.model.UI
import cc.cryptopunks.ui.poc.model.UIModel
import cc.cryptopunks.ui.poc.model.factory.invoke
import cc.cryptopunks.ui.poc.model.factory.uiContext
import cc.cryptopunks.ui.poc.stub.StubServiceInterface
import com.flipkart.android.proteus.Proteus
import com.flipkart.android.proteus.ProteusBuilder
import com.flipkart.android.proteus.gson.ProteusTypeAdapterFactory
import com.flipkart.android.proteus.support.design.DesignModule
import com.flipkart.android.proteus.support.v4.SupportV4Module
import com.flipkart.android.proteus.support.v7.CardViewModule
import com.flipkart.android.proteus.support.v7.RecyclerViewModule
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val app get() = App.instance

class App : Application() {

    companion object {
        lateinit var instance: App private set
    }

    private val scope = MainScope()

    override fun onCreate() {
        super.onCreate()
        instance = this
        proteus
        gson
        schemaUpdates
    }

    val service: Service.Interface by lazy {
        StubServiceInterface()
    }

    val model by lazy {
        UIModel(UI.State(service))
    }

    val schemaUpdates by lazy {
        scope.launch {
            service.schemas()
                .map(Service.Schema::uiContext)
                .map(UI.Action::AddContext)
                .collect(model::handle)
        }
    }

    val proteus: Proteus by lazy {
        ProteusBuilder()
            .register(SupportV4Module.create())
            .register(RecyclerViewModule.create())
            .register(CardViewModule.create())
            .register(DesignModule.create())
            .build()
    }

    val proteusTypeAdapterFactory by lazy {
        ProteusTypeAdapterFactory.PROTEUS_INSTANCE_HOLDER.proteus = proteus
        ProteusTypeAdapterFactory(this)
    }

    val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapterFactory(proteusTypeAdapterFactory)
            .create()
    }
}
