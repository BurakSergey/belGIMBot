import dev.inmo.tgbotapi.types.message.content.TextMessage

data class OrderInfo(
    var clientCode: TextMessage? = null,
    var orderCode: TextMessage? = null,
    var orderData: TextMessage? = null
)
