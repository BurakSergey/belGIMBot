package by.belgim

interface MessagesDao {
    suspend fun getAllMessages(): List<UserMessage>
    suspend fun addMessageToDB(userMessage: UserMessage) : UserMessage?
    suspend fun getMessage(id : Int) : UserMessage?
    suspend fun updateMessage(id: Int,answer: String): Boolean
}