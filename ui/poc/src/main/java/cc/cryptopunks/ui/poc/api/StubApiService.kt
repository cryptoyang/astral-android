package cc.cryptopunks.ui.poc.api

import cc.cryptopunks.ui.poc.schema.rpc.Rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private var lastMessageId = 0
private var lastContactId = 0

private fun nextMessageId() = MessengerApi.Message.Id(lastMessageId++.toString())
private fun nextContactId() = MessengerApi.Contact.Id(lastContactId++.toString())

val contacts = listOf(
    MessengerApi.Contact(nextContactId(), "User"),

    MessengerApi.Contact(nextContactId(), "Joe"),
    MessengerApi.Contact(nextContactId(), "Mike"),
    MessengerApi.Contact(nextContactId(), "Bob"),
    MessengerApi.Contact(nextContactId(), "Alice"),
)

private val contactsMap = contacts.associateBy { it.id }

private val messages = listOf(
    MessengerApi.Message(
        id = nextMessageId(),
        from = contacts[1].id,
        to = contacts[0].id,
        text = "Yo"
    ),
    MessengerApi.Message(
        id = nextMessageId(),
        from = contacts[4].id,
        to = contacts[0].id,
        text = "Whats up!?"
    ),
    MessengerApi.Message(
        id = nextMessageId(),
        from = contacts[0].id,
        to = contacts[3].id,
        text = "How are you doing?"
    ),
)

private val overview: List<MessengerApi.Overview.Item> = messages.map {
    MessengerApi.Overview.Item(
        contact = it.contact,
        lastMessage = it
    )
}

private val MessengerApi.Message.contact
    get() = listOf(from, to)
        .minus(contacts[0].id).first()
        .let(contactsMap::getValue)

fun handle(exec: Rpc.Command): Flow<Any> = flowOf(
    when (exec) {
        is MessengerApi.GetOverview -> exec { overview }
        is MessengerApi.GetContacts -> exec { MessengerApi.Contacts(contacts) }
        is MessengerApi.GetMessages -> exec { messages.filter { it.contact.id == exec.id } }
        is MessengerApi.SendMessage -> Unit
//    is MessengerApi.StartConversation -> exec {
//        MessengerApi.Conversation(
//            contact = contactsMap[id]!!,
//            messages = messages.filter { it.contact.id == exec.id }
//        )
//    }
        else -> Unit
    }
)

private operator fun <M: Rpc.Listen<R>, R> M.invoke(block: M.() -> R) = block()

fun main() {
    overview.forEach {
        println(it)
    }
}
