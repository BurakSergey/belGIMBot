import kotlinx.serialization.Serializable

@Serializable
data class ResponseOrderInfo(
    val code : String,
    val quantity: Int,
    val canceled_quantity: Int,
    val canceled_note: String,
    val ready: Boolean,
    val end_date: String,
    val returned: Boolean,
    val returned_quantity: Int
)