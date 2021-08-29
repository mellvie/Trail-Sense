package com.kylecorry.trail_sense

import android.content.Context
import com.kylecorry.andromeda.notify.Notify
import com.kylecorry.trail_sense.astronomy.infrastructure.SunsetAlarmService
import com.kylecorry.trail_sense.tools.backtrack.infrastructure.services.BacktrackAlwaysOnService
import com.kylecorry.trail_sense.tools.clock.infrastructure.NextMinuteBroadcastReceiver
import com.kylecorry.trail_sense.tools.flashlight.infrastructure.FlashlightService
import com.kylecorry.trail_sense.tools.flashlight.infrastructure.SosService
import com.kylecorry.trail_sense.tools.flashlight.infrastructure.StrobeService
import com.kylecorry.trail_sense.tools.speedometer.infrastructure.PedometerService
import com.kylecorry.trail_sense.tools.waterpurification.infrastructure.WaterPurificationTimerService
import com.kylecorry.trail_sense.tools.whitenoise.infrastructure.WhiteNoiseService
import com.kylecorry.trail_sense.weather.infrastructure.services.WeatherUpdateService

object NotificationChannels {

    const val GROUP_UPDATES = "trail_sense_updates"
    const val GROUP_FLASHLIGHT = "trail_sense_flashlight"
    const val GROUP_WEATHER = "trail_sense_weather"
    const val GROUP_DAILY_WEATHER = "trail_sense_daily_weather"
    const val GROUP_STORM = "trail_sense_storm"
    const val GROUP_SUNSET = "trail_sense_sunset"
    const val GROUP_PEDOMETER = "trail_sense_pedometer"
    const val GROUP_WATER = "trail_sense_water"
    const val GROUP_CLOCK = "trail_sense_clock"

    const val CHANNEL_BACKGROUND_UPDATES = "background_updates"

    fun createChannels(context: Context) {
        // Flashlight
        Notify.createChannel(
            context,
            StrobeService.CHANNEL_ID,
            context.getString(R.string.flashlight_title),
            context.getString(R.string.flashlight_title),
            Notify.CHANNEL_IMPORTANCE_LOW,
            muteSound = true
        )

        Notify.createChannel(
            context,
            FlashlightService.CHANNEL_ID,
            context.getString(R.string.flashlight_title),
            context.getString(R.string.flashlight_title),
            Notify.CHANNEL_IMPORTANCE_LOW,
            muteSound = true
        )

        Notify.createChannel(
            context,
            SosService.CHANNEL_ID,
            context.getString(R.string.flashlight_title),
            context.getString(R.string.flashlight_title),
            Notify.CHANNEL_IMPORTANCE_LOW,
            muteSound = true
        )


        // Backtrack
        Notify.createChannel(
            context,
            BacktrackAlwaysOnService.FOREGROUND_CHANNEL_ID,
            context.getString(R.string.backtrack),
            context.getString(R.string.backtrack_notification_channel_description),
            Notify.CHANNEL_IMPORTANCE_LOW,
            muteSound = true
        )

        // Background updates
        Notify.createChannel(
            context,
            CHANNEL_BACKGROUND_UPDATES,
            context.getString(R.string.updates),
            context.getString(R.string.updates),
            Notify.CHANNEL_IMPORTANCE_LOW,
            muteSound = true
        )

        // Clock
        Notify.createChannel(
            context,
            NextMinuteBroadcastReceiver.CHANNEL_ID,
            context.getString(R.string.notification_channel_clock_sync),
            context.getString(R.string.notification_channel_clock_sync_description),
            Notify.CHANNEL_IMPORTANCE_HIGH,
            false
        )

        // Water boil timer
        Notify.createChannel(
            context,
            WaterPurificationTimerService.CHANNEL_ID,
            context.getString(R.string.water_boil_timer),
            context.getString(R.string.water_boil_timer_channel_description),
            Notify.CHANNEL_IMPORTANCE_HIGH,
            false
        )

        // White noise
        Notify.createChannel(
            context,
            WhiteNoiseService.NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.tool_white_noise_title),
            context.getString(R.string.tool_white_noise_title),
            Notify.CHANNEL_IMPORTANCE_LOW
        )

        // Storm alert
        Notify.createChannel(
            context,
            WeatherUpdateService.STORM_CHANNEL_ID,
            context.getString(R.string.notification_storm_alert_channel_name),
            context.getString(R.string.storm_alerts),
            Notify.CHANNEL_IMPORTANCE_HIGH
        )

        Notify.createChannel(
            context,
            WeatherUpdateService.WEATHER_CHANNEL_ID,
            context.getString(R.string.weather),
            context.getString(R.string.notification_monitoring_weather),
            Notify.CHANNEL_IMPORTANCE_LOW,
            true
        )

        Notify.createChannel(
            context,
            WeatherUpdateService.DAILY_CHANNEL_ID,
            context.getString(R.string.todays_forecast),
            context.getString(R.string.todays_forecast),
            Notify.CHANNEL_IMPORTANCE_LOW,
            true
        )

        // Sunset
        Notify.createChannel(
            context,
            SunsetAlarmService.NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.sunset_alert_channel_title),
            context.getString(R.string.sunset_alerts),
            Notify.CHANNEL_IMPORTANCE_HIGH,
            false
        )

        // Odometer
        Notify.createChannel(
            context,
            PedometerService.CHANNEL_ID,
            context.getString(R.string.odometer),
            context.getString(R.string.odometer),
            Notify.CHANNEL_IMPORTANCE_LOW,
            true
        )
    }

}