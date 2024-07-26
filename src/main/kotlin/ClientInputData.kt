import dev.inmo.tgbotapi.types.message.content.TextMessage

data class ClientInputData(
    var clientCode: TextMessage? = null,
    var orderCode: TextMessage? = null,
    var orderDate: TextMessage? = null
)