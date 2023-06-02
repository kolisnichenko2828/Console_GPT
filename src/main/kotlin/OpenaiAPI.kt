import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

interface OpenAIApiService {
    @POST("v1/chat/completions")
    suspend fun generateText(
        @Header("Authorization") apiKey: String,
        @Body body: GenerateTextBody
    ): Response
}

data class GenerateTextBody(val max_tokens: Int,
                            val n: Int,
                            val messages: Array<Map<String, String>>,
                            val temperature: Int = 1,
                            val model: String = "gpt-3.5-turbo")

data class Response(
    val id: String,
    val `object`: String,
    val created: Double,
    val model: String,
    val usage: Usage,
    val choices: List<Choice>
)

data class Usage(
    val prompt_tokens: Double,
    val completion_tokens: Double,
    val total_tokens: Double
)

data class Choice(
    val message: Message,
    val finish_reason: String,
    val index: Double
)

data class Message(
    val role: String,
    val content: String
)

class OpenaiAPI {
    fun chatGPT(text: String): String {
        try {
            val okHttpClient = OkHttpClient.Builder()
                .callTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            val service = retrofit.create(OpenAIApiService::class.java)

            var content: String
            runBlocking {
                val apiKey = "Bearer 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"
                val messages = arrayOf(
                    // mapOf("role" to "system", "content" to ""),
                    mapOf("role" to "user", "content" to text)
                    // mapOf("role" to "assistant", "content" to "привет")
                )
                val body = GenerateTextBody(2000, 1, messages)
                val response = service.generateText(apiKey, body)
                content = response.choices.first().message.content
            }
            return content
        } catch(e: HttpException) {
            println("code = ${e.code()} message = ${e.message()}")
            println("errorBody = ${e.response()?.errorBody()?.string()}")
            return "[ботик] ошибка"
        }
    }
}