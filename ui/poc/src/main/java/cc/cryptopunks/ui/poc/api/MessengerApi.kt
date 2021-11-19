package cc.cryptopunks.ui.poc.api

import cc.cryptopunks.ui.poc.schema.rpc.Rpc

object MessengerApi : Rpc.Api() {

    // Methods

    sealed interface Method

    object GetOverview : Method, Rpc.Return<List<Overview.Item>>()

    object GetContacts : Method, Rpc.Return<Contacts>()

    data class GetMessages(
        val id: Contact.Id,
    ) : Method, Rpc.Return<List<Message>>()

    data class StartConversation(
        val id: Contact.Id,
    ) : Method, Rpc.Return<Conversation>()

    data class SendMessage(
        val id: Contact.Id,
        val text: String,
    ) : Method, Rpc.Method

    data class ListenMessages(
        val on: Boolean,
        val event: Event,
    ) : Method, Rpc.Subscribe<List<Message>>() {
        enum class Event { MessageReceived }
    }

    // Data

    data class Message(
        val id: Id,
        val from: Contact.Id,
        val to: Contact.Id,
        val text: String,
    ) {
        data class Id(val value: String)
    }

    data class Conversation(
        val contact: Contact,
        val messages: List<Message>
    )

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
