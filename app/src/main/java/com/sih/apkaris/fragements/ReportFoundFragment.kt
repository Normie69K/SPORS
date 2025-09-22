package com.sih.apkaris.fragements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sih.apkaris.MainActivity
import com.sih.apkaris.adapters.ChatListAdapter
import com.sih.apkaris.databinding.FragmentReportFoundBinding
import com.sih.apkaris.models.ChatSession

class ReportFoundFragment : Fragment() {

    private var _binding: FragmentReportFoundBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatListAdapter: ChatListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportFoundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.buttonReportFound.setOnClickListener {
            // In a real app, this would open a dialog to enter a device ID first
            (activity as? MainActivity)?.navigateTo(AnonymousChatFragment())
        }

        binding.buttonPastChats.setOnClickListener {
            // This might navigate to a different screen if the list is very long
            Toast.makeText(requireContext(), "Showing all past chats", Toast.LENGTH_SHORT).show()
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        // This is dummy data. In a real app, you would fetch this from your database.
        val dummyChatList = listOf(
            ChatSession(
                deviceName = "VIVO T1",
                lastMessage = "Hello thanks for finding my VIVO T1 Plea...",
                timestamp = "11:42 am",
                sessionId = "chat123"
            )
        )

        chatListAdapter = ChatListAdapter(dummyChatList) { session ->
            // Handle click on a chat item
            val mainActivity = activity as? MainActivity
            mainActivity?.navigateTo(AnonymousChatFragment.newInstance(session.sessionId))
        }

        binding.recyclerViewChats.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}