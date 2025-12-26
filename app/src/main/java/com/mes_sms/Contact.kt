package com.example.myapplication.mes_sms

// Simple contact data model
data class Contact(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val department: String,
    val city: String,
    val phoneNumber: String
)
