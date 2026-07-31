package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response DTOs ---

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null
)

data class GeminiGenerationConfig(
    val responseMimeType: String? = "application/json",
    val temperature: Float? = 0.2f
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

// --- AI Action Result JSON Model ---
data class AiActionParsed(
    val action: String, // "chat", "new_repair", "repair_update", "finance", "sell"
    val message: String? = null,
    val vrno: Long? = null,
    val searchName: String? = null,
    val searchModel: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val model: String? = null,
    val issue: String? = null,
    val status: String? = null,
    val income: Double? = null,
    val cost: Double? = null,
    val type: String? = null, // "ဝင်ငွေ" or "ထွက်ငွေ"
    val group: String? = null,
    val amount: Double? = null,
    val itemName: String? = null,
    val qty: Int? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun interpretCommand(userText: String): AiActionParsed? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return AiActionParsed(
                action = "chat",
                message = "API Key Not Configured. Please set your GEMINI_API_KEY in the AI Studio Secrets panel."
            )
        }

        val systemPrompt = """
            You are an AI Assistant for "Mobile ANSWER" Phone Repair & Shop in Myanmar.
            Parse the user's spoken or typed text (in Myanmar or English) into a structured JSON action object.
            Your output MUST be ONLY raw JSON without markdown or backticks.

            JSON Schema Options:
            1. New Repair:
            {
              "action": "new_repair",
              "name": "Customer Name",
              "model": "Phone Model",
              "issue": "Problem/Issue description"
            }

            2. Update Repair:
            {
              "action": "repair_update",
              "vrno": 101, (or null if searching by name/model)
              "searchName": "Customer Name",
              "searchModel": "Phone Model",
              "status": "စစ်ဆေးပြုပြင်နေဆဲ" | "ပြင်ပြီး-မရွေးသေး" | "ပြင်မရ-မရွေးသေး" | "ထုတ်ယူပြီး-အောင်မြင်" | "ထုတ်ယူပြီး-ပြင်မရ" | "ပယ်ဖျက် (Void)",
              "income": 35000.0,
              "cost": 20000.0
            }

            3. Finance Entry:
            {
              "action": "finance",
              "type": "ဝင်ငွေ" | "ထွက်ငွေ",
              "group": "Service" | "Sales" | "Office" | "Home" | "Personal",
              "name": "Description",
              "amount": 15000.0
            }

            4. Sell Inventory Item:
            {
              "action": "sell",
              "itemName": "Item Name",
              "qty": 1
            }

            5. Chat / Question / Greeting / General response:
            {
              "action": "chat",
              "message": "Friendly response in Myanmar language."
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = userText)))
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.1f
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrBlank()) {
                val cleanJson = jsonText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                val adapter = moshi.adapter(AiActionParsed::class.java)
                adapter.fromJson(cleanJson)
            } else {
                AiActionParsed(action = "chat", message = "ကျေးဇူးပြု၍ ပြန်လည်ပြောကြားပေးပါ။")
            }
        } catch (e: Exception) {
            AiActionParsed(action = "chat", message = "⚠️ Gemini AI Error: ${e.message}")
        }
    }

    suspend fun askTechnicalRepairAssistant(
        userQuery: String,
        repairContext: String? = null,
        chatHistory: List<GeminiContent> = emptyList()
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return "⚠️ Gemini API Key မရှိသေးပါ။ AI Studio Secrets panel တွင် GEMINI_API_KEY သတ်မှတ်ပေးပါ။"
        }

        val systemPromptText = """
            You are 'Mobile ANSWER AI Repair Master' — an expert mobile phone hardware & software diagnostic specialist for smartphones (iPhone, Samsung, Xiaomi, Vivo, Oppo, Realme, Infinix, Techno, etc.).
            You assist mobile repair technicians and store staff with precise technical diagnostics, component troubleshooting, and repair advice.

            Guidelines:
            1. Language: Answer in friendly, professional Myanmar language (mixed with standard technical English terms like VBUS, Multimeter, Short Circuit, Diode Mode, Reballing, IC, Flex Cable, LCD, BGA).
            2. Repair Focus: Give step-by-step troubleshooting workflows (Step 1, Step 2, Step 3...).
            3. Multimeter / Hardware Testing: Mention specific diode mode testing, voltage measurements (VBUS 5V, VBAT 4.2V), DC Power supply current draw behavior (e.g. 0.05A short vs 0.20A boot loop).
            4. Include sections:
               - 🔍 **Diagnostic Checklist**
               - 🛠️ **Required Tools**
               - ⚡ **Testing & Voltage Checks**
               - ⚠️ **Safety & Precautions**
            5. Keep answers well-structured with clear line breaks and bold headings.
            ${if (!repairContext.isNullOrBlank()) "Context device under repair: $repairContext" else ""}
        """.trimIndent()

        val contents = mutableListOf<GeminiContent>()
        // Add chat history if present
        contents.addAll(chatHistory.takeLast(6))
        contents.add(GeminiContent(parts = listOf(GeminiPart(text = userQuery))))

        val request = GeminiRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                responseMimeType = null, // plain text formatted answer
                temperature = 0.3f
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPromptText)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Gemini AI ထံမှ တုံ့ပြန်မှု မရရှိပါ။"
        } catch (e: Exception) {
            "⚠️ Gemini AI ချိတ်ဆက်မှု အဆင်မပြေပါ: ${e.localizedMessage ?: e.message}"
        }
    }
}
