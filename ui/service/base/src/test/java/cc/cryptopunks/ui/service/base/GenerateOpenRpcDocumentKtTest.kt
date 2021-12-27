package cc.cryptopunks.ui.service.base

import cc.cryptopunks.ui.mapper.Jackson
import cc.cryptopunks.ui.service.schema.OpenRpc
import cc.cryptopunks.ui.testing.openRpcSchemeJson
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test

class GenerateOpenRpcDocumentKtTest {

    @Test
    fun test1() {
        val doc = MessengerApi.generateOpenRpcDocument()
        val json = Jackson.jsonPrettyWriter.writeValueAsString(doc)
        val parsed = Jackson.jsonSlimMapper.readValue(json, OpenRpc.Document::class.java)
        println(json)
        println(doc)
        println(parsed)
        assert(doc == parsed)
    }

    @Test
    fun test2() {
        val doc = Jackson.jsonMapper.readValue<OpenRpc.Document>(openRpcSchemeJson)
        val json = Jackson.jsonPrettyWriter.writeValueAsString(doc)
        println(doc)
        println(json)
    }
}

internal object MessengerApi : Rpc.Api() {

    // Methods

    object GetOverview : Rpc.Listen<List<Overview.Item>>()

    object GetContacts : Rpc.Listen<Contacts>()

    data class GetMessages(
        val id: Contact.Id,
    ) : Rpc.Listen<List<Message>>()

    data class SendMessage(
        val id: Contact.Id,
        val text: String,
    ) : Rpc.Command

    // Data

    data class Message(
        val id: Id,
        val from: Contact.Id,
        val to: Contact.Id,
        val text: String,
        val time: Long = System.currentTimeMillis()
    ) {
        data class Id(val value: String)
    }

    data class Contact(
        val id: Id,
        val name: String,
    ) {
        data class Id(val value: String)
    }

    data class Contacts(
        val contacts: List<Contact>
    )

    object Overview {
        data class Item(
            val contact: Contact,
            val lastMessage: Message,
        )
    }
}
