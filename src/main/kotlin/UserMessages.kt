package by.belgim

import org.jetbrains.exposed.sql.Table

object UserMessages : Table("messages") {
    val message_id = integer("message_id").autoIncrement()
    val message_text = text("message_text")
    val user_id = varchar("user_id", 20)
    val created_at = varchar("created_at",20)
    val get_answer = bool("get_answer")

    override val primaryKey = PrimaryKey(message_id)
}