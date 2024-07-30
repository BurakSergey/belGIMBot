package by.belgim

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction


object DatabaseFactory {
    fun init() {
       val database =  Database.connect(
            url = "jdbc:postgresql://localhost:5432/belgim_bot",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )

        transaction(database) {
            SchemaUtils.create(UserMessages)
        }
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
