package com.example.myapplication.mes_sms

import com.example.myapplication.R

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.mes_sms.data.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var tvStats: TextView
    private lateinit var btnShortcut: Button
    private lateinit var btnContacts: Button

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        tvStats = findViewById(R.id.tvStats)
        btnShortcut = findViewById(R.id.btnShortcut)
        btnContacts = findViewById(R.id.btnContacts)

        btnSend.setOnClickListener {
            promptPinAndSend()
        }

        btnShortcut.setOnClickListener {
            ShortcutHelper.createShortcut(this)
        }

        btnContacts.setOnClickListener {
            startActivity(Intent(this, ContactListActivity::class.java))
        }

        checkAndRequestPermissions()

        val svc = Intent(this, BackgroundService::class.java)
        ContextCompat.startForegroundService(this, svc)

        observeStats()
    }

    private fun observeStats() {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            db.messageDao().getAllFlow().collectLatest { list ->
                val total = list.size
                val safe = list.count { it.state == "SAFE" }
                val notSafe = list.count { it.state == "NOT_SAFE" }
                val addresses = list.filter { it.state == "NOT_SAFE" && it.body.isNotBlank() }.size
                tvStats.text = "Stats: Messages sent: $total | Safe: $safe | Not Safe: $notSafe | Addresses: $addresses"
            }
        }
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
        val testNumber = "+10000000000"
        SmsManagerHelper.sendSms(this, testNumber, message)
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
