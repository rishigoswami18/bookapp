package com.hrishikeshbooks.bookapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hrishikeshbooks.bookapp.R
import com.hrishikeshbooks.bookapp.models.ChatModel

class ChatAdapter(private val messages: List<ChatModel>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val userText: TextView = view.findViewById(R.id.userText)
        val botText: TextView = view.findViewById(R.id.botText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        if (message.isUser) {
            holder.userText.text = message.message
            holder.userText.visibility = View.VISIBLE
            holder.botText.visibility = View.GONE
        } else {
            holder.botText.text = message.message
            holder.botText.visibility = View.VISIBLE
            holder.userText.visibility = View.GONE
        }
    }

    override fun getItemCount() = messages.size
}
