package com.example.myapplication.mes_sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.myapplication.mes_sms.data.MessageEntity
import com.example.myapplication.mes_sms.data.AppDatabase

class SmsReceiver : BroadcastReceiver() {
    private val TAG = "SmsReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return
            val sb = StringBuilder()
            var sender: String? = null
            for (msg in messages) {
                sender = msg.originatingAddress ?: sender
                sb.append(msg.messageBody)
            }
            val body = sb.toString().trim()
            Log.d(TAG, "Received SMS from $sender: $body")

            // Launch background coroutine to process without blocking the receiver
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    // Use suspend ContactStore API
                    val contact = ContactStore.findByNumberSuspend(context, sender ?: "")
                    if (contact == null) {
                        Log.d(TAG, "Ignoring SMS from unknown number: $sender")
                        return@launch
                    }

                    // Start foreground service
                    val serviceIntent = Intent(context, BackgroundService::class.java)
                    context.startForegroundService(serviceIntent)

                    val state = when (body) {
                        "1" -> MessageState.SAFE
                        "2" -> {
                            val followUp = context.getString(R.string.follow_up)
                            SmsManagerHelper.sendSms(context, sender ?: "", followUp)
                            MessageState.NOT_SAFE
                        }
                        else -> MessageState.NO_RESPONSE
                    }

                    // Persist message
                    db.messageDao().insert(MessageEntity(contactName = contact.firstName, phoneNumber = sender ?: "", state = state.name, body = body, timestamp = System.currentTimeMillis()))

                    // CSV export
                    val timestamp = System.currentTimeMillis().toString()
                    ExcelHelper.appendResponsesToCsv(context, "replies", listOf(listOf(contact.firstName, sender ?: "", state.name, body, timestamp)))

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing incoming SMS in coroutine: ${e.message}")
                }
            }

        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error processing incoming SMS: ${e.message}")
        }
    }
}
