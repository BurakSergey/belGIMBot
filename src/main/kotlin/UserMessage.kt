package by.belgim

import java.util.*

data class UserMessage(
    val message_id: Int,
    val message_text: String,
    val user_id : String,
    val created_at: String = Constant.sdf.format(Date()).toString(),
    val get_answer: Boolean = false
)