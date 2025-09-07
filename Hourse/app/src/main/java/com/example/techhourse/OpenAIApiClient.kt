package com.example.techhourse

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// 使用单例模式避免资源竞争
class OpenAIApiClient private constructor() {

    companion object {
        private const val API_KEY = "sk-a1ea1c4273524e38a2af8718abe3f595"
        
        @Volatile
        private var INSTANCE: OpenAIApiClient? = null
        
        fun getInstance(): OpenAIApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OpenAIApiClient().also { INSTANCE = it }
            }
        }
        
        fun isApiKeyConfigured(): Boolean {
            return API_KEY.isNotEmpty() && API_KEY.startsWith("sk-")
        }
    }

    private val apiService: OpenAIApiService

    init {
        // 配置OkHttp客户端，设置超时时间
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
            
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(OpenAIApiService::class.java)
    }

    suspend fun chatCompletion(message: String, systemPrompt: String? = null): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("OpenAIApiClient", "Starting API request...")
                Log.d("OpenAIApiClient", "Message length: ${message.length}")
                Log.d("OpenAIApiClient", "SystemPrompt length: ${systemPrompt?.length ?: 0}")
                
                val messages = mutableListOf<ChatMessage>()
                
                // 添加系统提示词（如果提供）
                systemPrompt?.let {
                    messages.add(
                        ChatMessage(
                            role = "system",
                            content = it
                        )
                    )
                    Log.d("OpenAIApiClient", "Added system prompt")
                }
                
                // 添加用户消息
                messages.add(
                    ChatMessage(
                        role = "user",
                        content = message
                    )
                )
                Log.d("OpenAIApiClient", "Added user message")
                
                val requestBody = OpenAIChatRequest(
                    model = "qwen-plus",
                    messages = messages
                )
                
                // 计算总的字符数
                val totalChars = messages.sumOf { it.content.length }
                Log.d("OpenAIApiClient", "Total characters in request: $totalChars")
                Log.d("OpenAIApiClient", "Total messages count: ${messages.size}")

                Log.d("OpenAIApiClient", "Making API call to Qwen Plus...")
                // 使用 Retrofit 的异步执行方法
                val response = apiService.chatCompletion("Bearer $API_KEY", requestBody)
                Log.d("OpenAIApiClient", "API response received: ${response.code()}")

                if (response.isSuccessful) {
                    val openAIResponse = response.body()
                    val text = openAIResponse?.choices?.firstOrNull()?.message?.content
                    Log.d("OpenAIApiClient", "Successful response: $text")
                    text?.trim() ?: "抱歉，AI暂时无法回复。"
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("OpenAIApiClient", "API Error: ${response.code()} - $errorBody")
                    when (response.code()) {
                        401 -> "API密钥无效，请检查配置"
                        403 -> "API访问被拒绝，请检查权限"
                        429 -> "请求过于频繁，请稍后再试"
                        else -> "请求失败：HTTP ${response.code()}"
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("OpenAIApiClient", "Socket timeout in chatCompletion", e)
                "网络连接超时，请检查网络设置"
            } catch (e: java.net.UnknownHostException) {
                Log.e("OpenAIApiClient", "Unknown host in chatCompletion", e)
                "无法连接到服务器，请检查网络连接"
            } catch (e: Exception) {
                Log.e("OpenAIApiClient", "Exception in chatCompletion", e)
                "发生错误：${e.message}"
            }
        }
    }
}

interface OpenAIApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIChatRequest
    ): Response<OpenAIChatResponse>
}

// 请求和响应数据类
data class OpenAIChatRequest(
    val model: String,
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class OpenAIChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)
