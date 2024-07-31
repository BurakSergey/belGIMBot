import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    @SerializedName("token")
    val token: String,
   @SerializedName("image_path")
    val imagePath: String,
   @SerializedName("admin_password")
    val adminPassword: String,
   @SerializedName("postgres")
    val postgres: PostgresParameters
)