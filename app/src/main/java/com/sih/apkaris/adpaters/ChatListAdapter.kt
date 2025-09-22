package com.sih.apkaris.adapters // You can create a new 'adapters' package

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sih.apkaris.databinding.ListItemChatBinding
import com.sih.apkaris.models.ChatSession

class ChatListAdapter(
    private val chatList: List<ChatSession>,
    private val onItemClicked: (ChatSession) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ListItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: ChatSession) {
            binding.tvDeviceName.text = session.deviceName
            binding.tvMessagePreview.text = session.lastMessage
            binding.tvTimestamp.text = session.timestamp
            binding.root.setOnClickListener { onItemClicked(session) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ListItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount() = chatList.size
}