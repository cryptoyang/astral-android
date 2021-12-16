package cc.cryptopunks.ui.proteus

import android.content.Context
import com.flipkart.android.proteus.Proteus
import com.flipkart.android.proteus.ProteusBuilder
import com.flipkart.android.proteus.gson.ProteusTypeAdapterFactory
import com.flipkart.android.proteus.support.design.DesignModule
import com.flipkart.android.proteus.support.v4.SupportV4Module
import com.flipkart.android.proteus.support.v7.CardViewModule
import com.flipkart.android.proteus.support.v7.RecyclerViewModule
import com.google.gson.Gson
import com.google.gson.GsonBuilder

lateinit var proteusComponent: ProteusComponent

class ProteusComponent(
    context: Context
) {
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
        ProteusTypeAdapterFactory(context)
    }

    val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapterFactory(proteusTypeAdapterFactory)
            .create()
    }
}
