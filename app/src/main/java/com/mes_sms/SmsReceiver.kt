package com.example.myapplication.mes_sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

// BroadcastReceiver to capture incoming SMS and route to app logic.
class SmsReceiver : BroadcastReceiver() {
    private val TAG = "SmsReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val bundle: Bundle? = intent.extras
            if (bundle == null) return
            val pdusObj = bundle.get("pdus") as? Array<*>
            if (pdusObj == null) return
            val sb = StringBuilder()
            var sender = ""
            for (pdu in pdusObj) {
                val format = bundle.getString("format")
                val bytes = pdu as? ByteArray ?: continue
                val msg = if (format != null) SmsMessage.createFromPdu(bytes, format) else SmsMessage.createFromPdu(bytes)
                sender = msg.originatingAddress ?: sender
                sb.append(msg.messageBody)
            }
            val body = sb.toString()
            Log.d(TAG, "Received SMS from $sender: $body")

            // Start background service to ensure app stays alive
            val serviceIntent = Intent(context, BackgroundService::class.java)
            context.startForegroundService(serviceIntent)

            // Save reply handling would go here; for now just log and write a CSV row.
            ExcelHelper.appendResponsesToCsv(context, "replies", listOf(listOf(sender, body, System.currentTimeMillis().toString())))

        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error processing incoming SMS: ${e.message}")
        }
    }
}
