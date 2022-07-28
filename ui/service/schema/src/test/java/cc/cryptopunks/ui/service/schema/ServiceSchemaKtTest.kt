package cc.cryptopunks.ui.service.schema

import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.service.base.generateOpenRpcDocument
import cc.cryptopunks.ui.testing.MessengerApi
import cc.cryptopunks.ui.testing.openRpcSchemeJson
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Ignore
import org.junit.Test

class ServiceSchemaKtTest {

    @Test
    @Ignore // Fixme
    fun test1() {
        val doc = MessengerApi.generateOpenRpcDocument()
        val schema = doc.toSchema()
        println(schema)
    }

    @Test
    fun test2() {
        val doc = Jackson.jsonSlimMapper.readValue<OpenRpc.Document>(openRpcSchemeJson)
        val schema = doc.toSchema()
        println(schema)
    }
}
