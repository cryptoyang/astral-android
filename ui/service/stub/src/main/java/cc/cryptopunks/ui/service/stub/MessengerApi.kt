package cc.cryptopunks.ui.service.stub

import cc.cryptopunks.ui.service.base.Rpc

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
