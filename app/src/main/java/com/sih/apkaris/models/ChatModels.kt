package com.sih.apkaris.models // You can create a new 'models' package for this

// Represents a single chat session in the "Past conversations" list
data class ChatSession(
    val deviceName: String,
    val lastMessage: String,
    val timestamp: String,
    val sessionId: String // A unique ID for this chat
)

// Represents a single message inside the chat screen
data class ChatMessage(
    val messageText: String,
    val timestamp: String,
    val isSentByUser: Boolean // True if this message is outgoing, false if incoming
)