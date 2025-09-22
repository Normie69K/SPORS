package com.sih.apkaris.fragements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sih.apkaris.adpaters.ChatMessageAdapter
import com.sih.apkaris.databinding.FragmentAnonymousChatBinding
import com.sih.apkaris.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AnonymousChatFragment : Fragment() {

    private var _binding: FragmentAnonymousChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var messageAdapter: ChatMessageAdapter
    private val messageList = ArrayList<ChatMessage>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnonymousChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadInitialMessages()

        binding.buttonSend.setOnClickListener {
            sendMessage()
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = ChatMessageAdapter(messageList)
        binding.recyclerViewChatMessages.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun loadInitialMessages() {
        // This is dummy data. In a real app, you'd fetch this from your backend.
        val ownerMessage = ChatMessage(
            messageText = "Hello! Thank you for finding my vivo T1. Please let me know where you found it and we can arrange to meet safely.",
            timestamp = "11:42 am",
            isSentByUser = false // Incoming message from the owner
        )
        messageList.add(ownerMessage)
        messageAdapter.notifyDataSetChanged()
    }

    private fun sendMessage() {
        val messageText = binding.editTextMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val newMessage = ChatMessage(
                messageText = messageText,
                timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                isSentByUser = true // Outgoing message from the current user
            )
            messageList.add(newMessage)
            messageAdapter.notifyItemInserted(messageList.size - 1)
            binding.recyclerViewChatMessages.scrollToPosition(messageList.size - 1)
            binding.editTextMessage.text.clear()

            // TODO: Add code here to send the message to your backend server
        }
    }

    companion object {
        fun newInstance(sessionId: String): AnonymousChatFragment {
            val fragment = AnonymousChatFragment()
            val args = Bundle()
            args.putString("SESSION_ID", sessionId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}