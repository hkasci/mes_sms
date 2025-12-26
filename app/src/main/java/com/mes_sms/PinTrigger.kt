package com.example.myapplication.mes_sms

// Simple PIN check helper - stores the PIN in-memory for simplicity.
object PinTrigger {
    private const val REQUIRED_PIN = "123" // default PIN, in real app store securely

    fun checkPin(pin: String): Boolean {
        return pin == REQUIRED_PIN
    }
}
