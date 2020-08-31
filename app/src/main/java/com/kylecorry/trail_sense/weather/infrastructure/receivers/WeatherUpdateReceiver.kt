package com.kylecorry.trail_sense.weather.infrastructure.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.system.AlarmUtils
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.sensors.*
import com.kylecorry.trail_sense.shared.system.IntentUtils
import com.kylecorry.trail_sense.shared.system.NotificationUtils
import com.kylecorry.trail_sense.weather.domain.PressureAltitudeReading
import com.kylecorry.trail_sense.weather.domain.WeatherService
import com.kylecorry.trail_sense.weather.domain.forcasting.Weather
import com.kylecorry.trail_sense.weather.domain.sealevel.SeaLevelPressureConverterFactory
import com.kylecorry.trail_sense.weather.infrastructure.WeatherNotificationService
import com.kylecorry.trail_sense.weather.infrastructure.database.PressureHistoryRepository
import java.time.*
import java.util.*
import kotlin.concurrent.timer

class WeatherUpdateReceiver : BroadcastReceiver() {

    private lateinit var context: Context
    private lateinit var barometer: IBarometer
    private lateinit var altimeter: IAltimeter
    private lateinit var sensorService: SensorService
    private lateinit var timeout: Timer

    private var hasAltitude = false
    private var hasBarometerReading = false

    private lateinit var userPrefs: UserPreferences
    private lateinit var weatherService: WeatherService

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "Broadcast received at ${ZonedDateTime.now()}")
        if (context == null) {
            return
        }

        this.context = context
        userPrefs = UserPreferences(context)
        weatherService = WeatherService(
            userPrefs.weather.stormAlertThreshold,
            userPrefs.weather.dailyForecastSlowThreshold,
            userPrefs.weather.hourlyForecastFastThreshold
        )

        sensorService = SensorService(context)
        barometer = sensorService.getBarometer()
        altimeter = sensorService.getAltimeter()

        start(intent)
    }

    private fun start(intent: Intent?) {
        scheduleNextAlarm(intent)
        if (!hasNotification()) {
            sendWeatherNotification()
        }

        if (!canRun()) {
            return
        }

        setLastUpdatedTime()
        setAltimeterTimeout(10000L)
        startSensors()
    }

    private fun scheduleNextAlarm(receivedIntent: Intent?) {
        if (receivedIntent?.action != INTENT_ACTION && AlarmUtils.isAlarmRunning(
                context,
                PI_ID,
                alarmIntent(context)
            )
        ) {
            Log.i(TAG, "Next alarm already scheduled, not setting a new one")
            return
        }

        val alarmMinutes = 20L

        Log.i(TAG, "Next alarm set for ${LocalDateTime.now().plusMinutes(alarmMinutes)}")

        // Cancel existing alarm (if any)
        AlarmUtils.cancel(context, pendingIntent(context))

        // Schedule the new alarm
        AlarmUtils.set(
            context,
            LocalDateTime.now().plusMinutes(alarmMinutes),
            pendingIntent(context),
            exact = false,
            allowWhileIdle = true
        )
    }

    private fun hasNotification(): Boolean {
        return NotificationUtils.isNotificationActive(
            context,
            WeatherNotificationService.WEATHER_NOTIFICATION_ID
        )
    }

    private fun sendWeatherNotification() {
        val pressureConverter = SeaLevelPressureConverterFactory().create(context)
        val readings = pressureConverter.convert(PressureHistoryRepository.getAll(context))
        val forecast = weatherService.getHourlyWeather(readings)

        if (userPrefs.weather.shouldShowWeatherNotification) {
            WeatherNotificationService.updateNotificationForecast(context, forecast, readings)
        }
    }

    private fun canRun(): Boolean {
        val threshold = Duration.ofMinutes(5)
        val lastCalled = Duration.between(getLastUpdatedTime(), LocalDateTime.now())

        return lastCalled >= threshold
    }

    private fun getLastUpdatedTime(): LocalDateTime {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val raw = prefs.getString(LAST_CALLED_KEY, LocalDateTime.MIN.toString())
            ?: LocalDateTime.MIN.toString()
        return LocalDateTime.parse(raw)
    }

    private fun setLastUpdatedTime() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit {
            putString(LAST_CALLED_KEY, LocalDateTime.now().toString())
        }
    }

    private fun setAltimeterTimeout(millis: Long){
        timeout = timer(period = millis) {
            if (!hasAltitude) {
                hasAltitude = true
                if (hasBarometerReading) {
                    gotAllReadings()
                }
            }
            cancel()
        }
    }

    private fun startSensors(){
        if (altimeter.hasValidReading){
            onAltitudeUpdate()
        } else {
            altimeter.start(this::onAltitudeUpdate)
        }
        barometer.start(this::onPressureUpdate)
    }

    private fun onAltitudeUpdate(): Boolean {
        hasAltitude = true
        if (hasBarometerReading){
            gotAllReadings()
        }
        return false
    }

    private fun onPressureUpdate(): Boolean {
        hasBarometerReading = true
        if (hasAltitude) {
            gotAllReadings()
        }
        return false
    }

    private fun gotAllReadings() {
        stopSensors()
        stopTimeout()
        addNewPressureReading()
        sendStormAlert()
        sendWeatherNotification()
        Log.i(TAG, "Got all readings recorded at ${ZonedDateTime.now()}")
    }

    private fun stopSensors(){
        altimeter.stop(this::onAltitudeUpdate)
        barometer.stop(this::onPressureUpdate)
    }

    private fun stopTimeout(){
        timeout.cancel()
    }

    private fun addNewPressureReading() {
        PressureHistoryRepository.add(
            context,
            PressureAltitudeReading(
                Instant.now(),
                barometer.pressure,
                altimeter.altitude
            )
        )
    }

    private fun sendStormAlert() {
        createNotificationChannel()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val sentAlert = prefs.getBoolean(context.getString(R.string.pref_just_sent_alert), false)

        val pressureConverter = SeaLevelPressureConverterFactory().create(context)
        val readings = PressureHistoryRepository.getAll(context)
        val forecast = weatherService.getHourlyWeather(pressureConverter.convert(readings))

        if (forecast == Weather.Storm) {
            val shouldSend = userPrefs.weather.sendStormAlerts
            if (shouldSend && !sentAlert) {
                val notification = NotificationCompat.Builder(context, STORM_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alert)
                    .setContentTitle(context.getString(R.string.notification_storm_alert_title))
                    .setContentText(context.getString(R.string.notification_storm_alert_text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

                NotificationUtils.send(context, STORM_ALERT_NOTIFICATION_ID, notification)

                prefs.edit {
                    putBoolean(context.getString(R.string.pref_just_sent_alert), true)
                }
            }
        } else {
            NotificationUtils.cancel(context, STORM_ALERT_NOTIFICATION_ID)
            prefs.edit {
                putBoolean(context.getString(R.string.pref_just_sent_alert), false)
            }
        }
    }

    private fun createNotificationChannel() {
        NotificationUtils.createChannel(
            context, STORM_CHANNEL_ID,
            context.getString(R.string.notification_storm_alert_channel_name),
            context.getString(R.string.notification_storm_alert_channel_desc),
            NotificationUtils.CHANNEL_IMPORTANCE_HIGH
        )
    }

    companion object {

        private const val STORM_CHANNEL_ID = "Alerts";
        private const val TAG = "WeatherUpdateReceiver"
        private const val INTENT_ACTION = "com.kylecorry.trail_sense.ALARM_UPDATE_WEATHER"
        const val PI_ID = 84097413
        private const val STORM_ALERT_NOTIFICATION_ID = 74309823

        fun pendingIntent(context: Context): PendingIntent {
            return PendingIntent.getBroadcast(
                context,
                PI_ID,
                alarmIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun intent(context: Context): Intent {
            return Intent(context, WeatherUpdateReceiver::class.java)
        }

        private fun alarmIntent(context: Context): Intent {
            return IntentUtils.localIntent(context, INTENT_ACTION)
        }

        private val MAX_BAROMETER_READINGS = 8
        private val MAX_GPS_READINGS = 1

        private const val LAST_CALLED_KEY = "weatherLastUpdated"
    }
}