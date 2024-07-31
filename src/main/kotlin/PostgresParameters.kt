import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PostgresParameters(
    @SerializedName("connection_string")
    val connectionString: String,
    @SerializedName("port")
    val port: Int,
    @SerializedName("username")
    val user: String,
    @SerializedName("password")
    val password: String
)
