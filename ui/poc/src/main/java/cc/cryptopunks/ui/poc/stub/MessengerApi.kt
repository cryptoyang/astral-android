package cc.cryptopunks.ui.poc.stub

import cc.cryptopunks.ui.poc.transport.schema.rpc.Rpc

object MessengerApi : Rpc.Api() {

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

object MessengerApi2 : Rpc.Api() {

    object GetContacts :
        Rpc.Listen<List<Contact>>()

    data class GetMessages(
        val id: Contact.Id
    ) : Rpc.Listen<List<Message>>()

    data class SendMessage(
        val to: Contact.Id,
        val text: String,
    ) : Rpc.Command

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
