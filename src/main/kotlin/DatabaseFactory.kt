import by.belgim.UserMessages
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction


object DatabaseFactory {
    fun init(postgresParameters: PostgresParameters) {
       val database =  Database.connect(
            url = "jdbc:postgresql:${postgresParameters.connectionString}",
            driver = "org.postgresql.Driver",
            user = postgresParameters.user,
            password = postgresParameters.password
        )

        transaction(database) {
            SchemaUtils.create(UserMessages)
        }
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
