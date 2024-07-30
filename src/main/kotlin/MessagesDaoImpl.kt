package by.belgim

import by.belgim.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*

class MessagesDaoImpl: MessagesDao {
    override suspend fun getAllMessages(): List<UserMessage> = dbQuery {
        UserMessages.selectAll().map(::resultRowToUserMessages)
    }

    override suspend fun addMessageToDB(userMessage: UserMessage) :UserMessage? = dbQuery {
        val insertStatement = UserMessages.insert {
            it[message_text] = userMessage.message_text
            it[user_id] = userMessage.user_id
            it[created_at] = userMessage.created_at
            it[get_answer] = userMessage.get_answer
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToUserMessages)
    }

    override suspend fun getMessage(id: Int): UserMessage? = dbQuery {
        UserMessages.select{
            UserMessages.message_id eq id
        }
            .map(::resultRowToUserMessages)
            .singleOrNull()
    }

    override suspend fun updateMessage(id: Int): Boolean = dbQuery {
        UserMessages.update( {UserMessages.message_id eq id}) {
            it[get_answer] = true
        } > 0
    }

    private fun resultRowToUserMessages(row: ResultRow) = UserMessage(
        message_id = row[UserMessages.message_id],
        message_text = row[UserMessages.message_text],
        user_id = row[UserMessages.user_id],
        created_at = row[UserMessages.created_at],
        get_answer = row[UserMessages.get_answer]
    )
}