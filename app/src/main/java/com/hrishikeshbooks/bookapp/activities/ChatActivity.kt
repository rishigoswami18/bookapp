package com.hrishikeshbooks.bookapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hrishikeshbooks.bookapp.R
import com.hrishikeshbooks.bookapp.adapter.ChatAdapter
import com.hrishikeshbooks.bookapp.databinding.ActivityChatBinding
import com.hrishikeshbooks.bookapp.models.ChatModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var input: EditText
    private lateinit var sendBtn: ImageButton
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.chatRecyclerView)
        input = findViewById(R.id.chatInput)
        sendBtn = findViewById(R.id.sendButton)

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        sendBtn.setOnClickListener {
            val userText = input.text.toString().trim()
            if (userText.isNotEmpty()) {
                addMessage(userText, true)
                callOpenAI(userText)
                input.text.clear()
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatModel(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun callOpenAI(prompt: String) {
        val client = OkHttpClient()

        val json = """
            {
              "model": "openai/gpt-3.5-turbo-0613",
              "messages": [
                {"role": "user", "content": "$prompt"}
              ]
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer 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")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    addMessage("Failed to reach AI: ${e.message}", false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("API_RESPONSE", "HTTP Code: ${response.code}, Body: $body")

                try {
                    if (body != null && body.trim().startsWith("{")) {
                        val jsonObject = JSONObject(body)

                        if (jsonObject.has("error")) {
                            val errorMsg = jsonObject.getJSONObject("error").getString("message")
                            runOnUiThread {
                                addMessage("❌ API Error: $errorMsg", false)
                            }
                        } else {
                            val reply = jsonObject
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")

                            runOnUiThread {
                                addMessage(reply.trim(), false)
                            }
                        }
                    } else {
                        runOnUiThread {
                            addMessage("❌ Unexpected response format (not JSON)", false)
                        }
                        Log.e("API_ERROR", "Non-JSON response: $body")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        addMessage("Oops! AI response failed to parse.\n${e.message}", false)
                    }
                }
            }
        })
    }
}
