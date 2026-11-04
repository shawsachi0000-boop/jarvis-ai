package com.javris.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AssistantActions {

    fun run(context: Context, command: String): String {
        val q = command.lowercase(Locale.ROOT)

        return try {
            when {
                "hello" in q || "namaste" in q ->
                    "Namaste! Main JAVRIS hoon."

                "time" in q || "samay" in q ->
                    "Abhi " + SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(Date())

                "date" in q || "tarikh" in q ->
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                    ).format(Date())

                "battery" in q -> {
                    val battery = context.getSystemService(
                        Context.BATTERY_SERVICE
                    ) as BatteryManager

                    "Battery ${battery.getIntProperty(
                        BatteryManager.BATTERY_PROPERTY_CAPACITY
                    )} percent hai."
                }

                "youtube" in q ->
                    open(context, "https://youtube.com")

                "whatsapp" in q ->
                    open(context, "https://wa.me/")

                "google" in q || "search" in q -> {
                    val search = command
                        .replace("Google", "", ignoreCase = true)
                        .replace("search", "", ignoreCase = true)
                        .trim()

                    open(
                        context,
                        "https://www.google.com/search?q=${Uri.encode(search)}"
                    )
                }

                "camera" in q -> {
                    context.startActivity(
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    "Camera khol raha hoon."
                }

                "alarm" in q -> {
                    context.startActivity(
                        Intent(AlarmClock.ACTION_SET_ALARM)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    "Alarm app khol diya."
                }

                "timer" in q -> {
                    context.startActivity(
                        Intent(AlarmClock.ACTION_SET_TIMER)
                            .putExtra(AlarmClock.EXTRA_LENGTH, 60)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    "One minute timer khol diya."
                }

                "wifi" in q ->
                    settings(context, "android.settings.WIFI_SETTINGS")

                "bluetooth" in q ->
                    settings(context, "android.settings.BLUETOOTH_SETTINGS")

                "setting" in q ->
                    settings(context, "android.settings.SETTINGS")

                "calculator" in q -> {
                    val i = context.packageManager
                        .getLaunchIntentForPackage(
                            "com.google.android.calculator"
                        )

                    if (i != null) {
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(i)
                        "Calculator khol diya."
                    } else {
                        "Calculator app nahi mili."
                    }
                }

                "call" in q || "phone" in q -> {
                    val number = Regex("[+]?[0-9][0-9 -]{5,}")
                        .find(command)?.value

                    if (number == null) {
                        "Number boliye, main dialer khol sakta hoon."
                    } else {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:$number")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        "Dialer khol diya. Call aap confirm karein."
                    }
                }

                else ->
                    "Ye command abhi supported nahi hai."
            }
        } catch (e: Exception) {
            "Action complete nahi hua. Phone settings check karein."
        }
    }

    private fun open(context: Context, url: String): String {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return "Opening now."
    }

    private fun settings(context: Context, action: String): String {
        context.startActivity(
            Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return "Settings khol di."
    }
}
