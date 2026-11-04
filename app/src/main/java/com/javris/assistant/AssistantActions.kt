package com.javris.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AssistantActions {

    fun run(context: Context, command: String): String {
        val text = command.lowercase(Locale.getDefault())

        return when {
            "time" in text || "समय" in text ->
                "Abhi time hai " +
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            "date" in text || "तारीख" in text ->
                "Aaj ki date hai " +
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

            "battery" in text || "बैटरी" in text -> {
                val manager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val level = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                "Battery $level percent hai."
            }

            "youtube" in text ->
                openApp(context, "com.google.android.youtube", "YouTube")

            "whatsapp" in text ->
                openApp(context, "com.whatsapp", "WhatsApp")

            "camera" in text || "कैमरा" in text ->
                launch(context, Intent("android.media.action.IMAGE_CAPTURE"), "Camera")

            "alarm" in text || "अलार्म" in text ->
                launch(context, Intent(AlarmClock.ACTION_SHOW_ALARMS), "Clock")

            "google" in text || "search" in text || "सर्च" in text -> {
                val query = Uri.encode(command)
                launch(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query")), "Browser")
            }

            "map" in text || "रास्ता" in text || "location" in text ->
                launch(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com")), "Maps")

            else -> "Ye command abhi mere paas available nahi hai."
        }
    }

    private fun openApp(context: Context, packageName: String, appName: String): String {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            context.startActivity(intent)
            "$appName khol raha hoon."
        } else {
            "$appName phone mein install nahi hai."
        }
    }

    private fun launch(context: Context, intent: Intent, name: String): String {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            "$name khol raha hoon."
        } catch (e: Exception) {
            "$name nahi khul paya."
        }
    }
}
