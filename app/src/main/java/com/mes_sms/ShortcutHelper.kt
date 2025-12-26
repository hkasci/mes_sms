package com.example.myapplication.mes_sms

import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon

// Simple helper to create a home screen shortcut (API 25+). Uses ShortcutManager if available.
object ShortcutHelper {
    fun createShortcut(activity: Activity) {
        val sm = activity.getSystemService(ShortcutManager::class.java) ?: return
        val shortcut = ShortcutInfo.Builder(activity, "mes_sms_shortcut")
            .setShortLabel("MES_SMS")
            .setLongLabel("Open MES_SMS")
            .setIcon(Icon.createWithResource(activity, android.R.drawable.ic_dialog_alert))
            .setIntent(Intent(activity, com.example.myapplication.mes_sms.MainActivity::class.java).setAction(Intent.ACTION_MAIN))
            .build()
        sm.dynamicShortcuts = listOf(shortcut)
    }
}
