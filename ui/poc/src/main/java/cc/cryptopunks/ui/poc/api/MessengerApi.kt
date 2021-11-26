package cc.cryptopunks.ui.poc.api

import cc.cryptopunks.ui.poc.schema.rpc.Rpc

object MessengerApi : Rpc.Api() {

    // Methods

    sealed interface Method

    object GetOverview : Method, Rpc.Single<List<Overview.Item>>()

    object GetContacts : Method, Rpc.Single<Contacts>()

    data class GetMessages(
        val id: Contact.Id,
    ) : Method, Rpc.Single<List<Message>>()

    data class SendMessage(
        val id: Contact.Id,
        val text: String,
    ) : Method, Rpc.Method

    data class ListenMessages(
        val on: Boolean,
        val event: Event,
    ) : Method, Rpc.Stream<List<Message>>() {
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

object MessengerApi2 : Rpc.Api() {

    object GetContacts :
        Rpc.Single<List<Contact>>()

    data class GetMessages(
        val id: Contact.Id
    ) : Rpc.Single<List<Message>>()

    data class SendMessage(
        val to: Contact.Id,
        val text: String,
    ) : Rpc.Method

    object SubscribeMessages :
        Rpc.Stream<Message>()

    data class Contact(
        val id: Id,
        val name: String,
        val group: List<Contact> = emptyList(),
    ) {
        data class Id(val value: String)
    }

    data class Message(
        val index: Int,
        val id: Id,
        val from: Contact,
        val to: Contact,
        val chat: Contact.Id,
        val text: String,
    ) {
        data class Id(val value: String)
    }
}
