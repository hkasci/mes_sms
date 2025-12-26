package com.example.myapplication.mes_sms

// MessageStatus represents reply tracking for a sent message
enum class MessageState { SENT, SAFE, NOT_SAFE, NO_RESPONSE }

data class MessageStatus(
    val contact: Contact,
    var state: MessageState = MessageState.SENT,
    var replyText: String? = null,
    var timestamp: Long = System.currentTimeMillis()
)

