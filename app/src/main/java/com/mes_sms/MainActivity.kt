package com.example.myapplication.mes_sms

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var tvStats: TextView
    private lateinit var btnShortcut: Button

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        tvStats = findViewById(R.id.tvStats)
        btnShortcut = findViewById(R.id.btnShortcut)

        btnSend.setOnClickListener {
            promptPinAndSend()
        }

        btnShortcut.setOnClickListener {
            ShortcutHelper.createShortcut(this)
        }

        checkAndRequestPermissions()

        // Start background service to listen for SMS
        val svc = Intent(this, BackgroundService::class.java)
        ContextCompat.startForegroundService(this, svc)
    }

    private fun checkAndRequestPermissions() {
        val missing = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 101)
        }
    }

    private fun promptPinAndSend() {
        val input = EditText(this)
        input.hint = getString(R.string.pin_prompt)
        AlertDialog.Builder(this)
            .setTitle("PIN")
            .setView(input)
            .setPositiveButton("OK") { dlg, _ ->
                val pin = input.text.toString().trim()
                if (PinTrigger.checkPin(pin)) {
                    sendEmergencyMessages()
                } else {
                    AlertDialog.Builder(this).setMessage("Invalid PIN").setPositiveButton("OK", null).show()
                }
                dlg.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendEmergencyMessages() {
        val message = etMessage.text.toString()
        // For simplicity: send to a hard-coded test number or TODO: select from contacts
        val testNumber = "+10000000000"
        SmsManagerHelper.sendSms(this, testNumber, message)
        tvStats.text = "Stats: Messages sent: 1 | Safe: 0 | Not Safe: 0 | Addresses: 0"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            AlertDialog.Builder(this)
                .setMessage("App needs SMS permissions to function. Open settings to grant?")
                .setPositiveButton("Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = android.net.Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
