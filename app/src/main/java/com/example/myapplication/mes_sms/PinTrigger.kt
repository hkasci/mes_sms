package com.example.myapplication.mes_sms

object PinTrigger {
    private const val REQUIRED_PIN = "123"
    fun checkPin(pin: String): Boolean = pin == REQUIRED_PIN
}

