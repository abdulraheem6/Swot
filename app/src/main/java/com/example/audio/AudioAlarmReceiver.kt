package com.example.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class AudioAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getStringExtra("EXTRA_SCHEDULE_ID") ?: return
        val scheduleName = intent.getStringExtra("EXTRA_SCHEDULE_NAME") ?: "Audio Reminder"
        val soundTone = intent.getStringExtra("EXTRA_SOUND_TONE") ?: "alarm"
        val customUri = intent.getStringExtra("EXTRA_CUSTOM_URI")
        val volume = intent.getIntExtra("EXTRA_VOLUME", 85)

        // 1. Acquire WakeLock to wake up and turn on screen even when locked
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "MyVault:AlarmWakeLock"
            )
            wakeLock.acquire(45_000L)
        } catch (_: Exception) {}

        // 2. Play continuous loud alarm ringtone
        AudioTonePlayer.playContinuousAlarm(context, soundTone, customUri, volume)

        // 3. Prepare full-screen intent for AlarmAlertActivity
        val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra("EXTRA_SCHEDULE_ID", scheduleId)
            putExtra("EXTRA_SCHEDULE_NAME", scheduleName)
            putExtra("EXTRA_SOUND_TONE", soundTone)
            putExtra("EXTRA_CUSTOM_URI", customUri)
            putExtra("EXTRA_VOLUME", volume)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.hashCode() + 2,
            alertIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Create Notification Channel with USAGE_ALARM
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "audio_reminder_alarm_channel_v3"
        val alarmSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Critical Alarms & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alarms and scheduled audio reminders that sound over lock screen"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setSound(alarmSound, audioAttributes)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 5. Build high priority notification with FullScreenIntent
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ ALARM: $scheduleName")
            .setContentText("Scheduled alarm triggered! Tap to open or snooze.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 800))
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        notificationManager.notify(scheduleId.hashCode(), notification)

        // 6. Directly launch AlarmAlertActivity so it opens right over lock screen
        try {
            context.startActivity(alertIntent)
        } catch (_: Exception) {}
    }
}
