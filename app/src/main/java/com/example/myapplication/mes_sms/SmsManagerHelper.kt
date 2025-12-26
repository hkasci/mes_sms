package com.example.myapplication.mes_sms

import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log

object SmsManagerHelper {
    private const val TAG = "SmsManagerHelper"

    fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            val sentIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent("SMS_SENT_ACTION"),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null)
            Log.d(TAG, "Sent SMS to $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS: ${e.message}")
        }
    }
}

