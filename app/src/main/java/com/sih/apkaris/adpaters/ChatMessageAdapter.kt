package com.sih.apkaris.adpaters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sih.apkaris.databinding.ListItemMessageIncomingBinding
import com.sih.apkaris.databinding.ListItemMessageOutgoingBinding
import com.sih.apkaris.models.ChatMessage

class ChatMessageAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_INCOMING = 1
    private val VIEW_TYPE_OUTGOING = 2

    // ViewHolder for incoming messages
    inner class IncomingMessageViewHolder(val binding: ListItemMessageIncomingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvMessageBody.text = message.messageText
        }
    }

    // ViewHolder for outgoing messages
    inner class OutgoingMessageViewHolder(val binding: ListItemMessageOutgoingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvMessageBody.text = message.messageText
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByUser) {
            VIEW_TYPE_OUTGOING
        } else {
            VIEW_TYPE_INCOMING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_INCOMING) {
            val binding = ListItemMessageIncomingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            IncomingMessageViewHolder(binding)
        } else {
            val binding = ListItemMessageOutgoingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            OutgoingMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is IncomingMessageViewHolder) {
            holder.bind(message)
        } else if (holder is OutgoingMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size
}