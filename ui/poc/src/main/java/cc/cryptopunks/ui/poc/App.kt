package cc.cryptopunks.ui.poc

import android.app.Application
import cc.cryptopunks.ui.poc.mapper.Jackson
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler
import com.flipkart.android.proteus.Proteus
import com.flipkart.android.proteus.ProteusBuilder
import com.flipkart.android.proteus.gson.ProteusTypeAdapterFactory
import com.flipkart.android.proteus.support.design.DesignModule
import com.flipkart.android.proteus.support.v4.SupportV4Module
import com.flipkart.android.proteus.support.v7.CardViewModule
import com.flipkart.android.proteus.support.v7.RecyclerViewModule
import com.google.gson.Gson
import com.google.gson.GsonBuilder

val app get() = App.instance

class App : Application() {
    companion object {
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        proteus
        gson
    }

    val proteus: Proteus by lazy {
        ProteusBuilder()
            .register(SupportV4Module.create())
            .register(RecyclerViewModule.create())
            .register(CardViewModule.create())
            .register(DesignModule.create())
            .build()
    }

    val gson: Gson by lazy {
        ProteusTypeAdapterFactory.PROTEUS_INSTANCE_HOLDER.proteus = proteus
        GsonBuilder()
            .registerTypeAdapterFactory(ProteusTypeAdapterFactory(this))
            .create()
    }
}

class ReferenceProblemHandler : DeserializationProblemHandler() {

    override fun handleUnknownProperty(
        ctxt: DeserializationContext?,
        p: JsonParser?,
        deserializer: JsonDeserializer<*>?,
        beanOrClass: Any?,
        propertyName: String,
    ): Boolean = propertyName == "\$ref"

    override fun handleInstantiationProblem(
        ctxt: DeserializationContext?,
        instClass: Class<*>?,
        argument: Any?,
        t: Throwable?,
    ): Any {
        return super.handleInstantiationProblem(ctxt, instClass, argument, t)
    }
}
