package cc.cryptopunks.ui.poc.api

import cc.cryptopunks.ui.poc.schema.rpc.Rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

private val messagesFlow = MutableSharedFlow<List<MessengerApi.Message>>(replay = 1).apply {
    tryEmit(
        listOf(
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
    )
}

private val overviewFlow: Flow<List<MessengerApi.Overview.Item>>
    get() = messagesFlow.map { list ->
        list.map {
            MessengerApi.Overview.Item(
                contact = it.contact,
                lastMessage = it
            )
        }
    }

private val MessengerApi.Message.contact
    get() = listOf(from, to)
        .minus(contacts[0].id).first()
        .let(contactsMap::getValue)

private val scope = CoroutineScope(Dispatchers.IO)

fun handle(exec: Rpc.Command): Flow<Any> =
    when (exec) {
        is MessengerApi.GetOverview -> overviewFlow
        is MessengerApi.GetContacts -> flowOf(MessengerApi.Contacts(contacts))
        is MessengerApi.GetMessages -> messagesFlow.map { messages -> messages.filter { it.contact.id == exec.id } }
        is MessengerApi.SendMessage -> {
            scope.launch {
                val message = MessengerApi.Message(
                    id = MessengerApi.Message.Id(lastMessageId++.toString()),
                    from = contacts.first().id,
                    to = exec.id,
                    text = exec.text
                )
                messagesFlow.apply {
                    val new = first() + message
                    emit(new)
                }
            }
            emptyFlow()
        }
        else -> emptyFlow()
    }

fun main() = runBlocking {
    overviewFlow.first().forEach {
        println(it)
    }
}
