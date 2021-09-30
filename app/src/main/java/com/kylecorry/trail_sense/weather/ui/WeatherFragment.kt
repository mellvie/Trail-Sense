package com.kylecorry.trail_sense.weather.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kylecorry.andromeda.core.sensors.asLiveData
import com.kylecorry.andromeda.core.time.Throttle
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.andromeda.location.IGPS
import com.kylecorry.sol.science.meteorology.forecast.Weather
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.PressureUnits
import com.kylecorry.sol.units.Reading
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.ActivityWeatherBinding
import com.kylecorry.trail_sense.quickactions.LowPowerQuickAction
import com.kylecorry.trail_sense.shared.*
import com.kylecorry.trail_sense.shared.sensors.SensorService
import com.kylecorry.trail_sense.shared.views.QuickActionNone
import com.kylecorry.trail_sense.shared.views.UserError
import com.kylecorry.trail_sense.tools.flashlight.ui.QuickActionFlashlight
import com.kylecorry.trail_sense.tools.whistle.ui.QuickActionWhistle
import com.kylecorry.trail_sense.weather.domain.*
import com.kylecorry.trail_sense.weather.infrastructure.WeatherContextualService
import com.kylecorry.trail_sense.weather.infrastructure.WeatherUpdateScheduler
import com.kylecorry.trail_sense.weather.infrastructure.clouds.CloudObservationRepo
import com.kylecorry.trail_sense.weather.infrastructure.persistence.PressureReadingEntity
import com.kylecorry.trail_sense.weather.infrastructure.persistence.PressureRepo
import com.kylecorry.trail_sense.weather.ui.observations.CloudObservationField
import com.kylecorry.trail_sense.weather.ui.observations.PressureObservationField
import com.kylecorry.trail_sense.weather.ui.observations.TemperatureObservationField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant

class WeatherFragment : BoundFragment<ActivityWeatherBinding>() {

    private val barometer by lazy { sensorService.getBarometer() }
    private val altimeter by lazy { sensorService.getGPSAltimeter() }
    private val thermometer by lazy { sensorService.getThermometer() }

    private var altitude = 0F
    private var useSeaLevelPressure = false
    private var units = PressureUnits.Hpa

    private val prefs by lazy { UserPreferences(requireContext()) }

    private lateinit var chart: PressureChart
    private lateinit var navController: NavController

    private lateinit var weatherService: WeatherService
    private val cloudService = CloudService()
    private val sensorService by lazy { SensorService(requireContext()) }
    private val formatService by lazy { FormatService(requireContext()) }
    private val pressureRepo by lazy { PressureRepo.getInstance(requireContext()) }
    private val cloudRepo by lazy { CloudObservationRepo.getInstance(requireContext()) }

    private val throttle = Throttle(20)

    private var pressureSetpoint: PressureAltitudeReading? = null

    private var readingHistory: List<PressureAltitudeReading> = listOf()

    private var valueSelectedTime = 0L

    private var leftQuickAction: QuickActionButton? = null
    private var rightQuickAction: QuickActionButton? = null

    private val weatherForecastService by lazy { WeatherContextualService.getInstance(requireContext()) }

    private var observations: List<Reading<WeatherObservation>> = emptyList()

    // TODO: This is temporary
    private var cloudObservations: List<Reading<WeatherObservation>> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leftQuickAction =
            getQuickActionButton(prefs.weather.leftQuickAction, binding.weatherLeftQuickAction)
        leftQuickAction?.onCreate()

        rightQuickAction =
            getQuickActionButton(prefs.weather.rightQuickAction, binding.weatherRightQuickAction)
        rightQuickAction?.onCreate()

        navController = findNavController()

        weatherService = WeatherService(
            prefs.weather.stormAlertThreshold,
            prefs.weather.dailyForecastChangeThreshold,
            prefs.weather.hourlyForecastChangeThreshold
        )

        chart = PressureChart(binding.chart) { timeAgo, pressure ->
            if (timeAgo == null || pressure == null) {
                binding.pressureMarker.text = ""
            } else {
                val formatted = formatService.formatPressure(
                    Pressure(pressure, units),
                    Units.getDecimalPlaces(units)
                )
                binding.pressureMarker.text = getString(
                    R.string.pressure_reading_time_ago,
                    formatted,
                    formatService.formatDuration(timeAgo, false)
                )
                valueSelectedTime = System.currentTimeMillis()
            }
        }

        binding.pressure.setOnLongClickListener {
            pressureSetpoint = if (pressureSetpoint == null) {
                PressureAltitudeReading(
                    Instant.now(),
                    barometer.pressure,
                    altimeter.altitude,
                    thermometer.temperature,
                    if (altimeter is IGPS) (altimeter as IGPS).verticalAccuracy else null
                )
            } else {
                null
            }

            prefs.weather.pressureSetpoint = pressureSetpoint

            pressureSetpoint?.let {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        pressureRepo.addPressure(PressureReadingEntity.from(it))
                    }
                }
            }

            true
        }

        pressureRepo.getPressures().observe(viewLifecycleOwner) {
            readingHistory = it.map { it.toPressureAltitudeReading() }.sortedBy { it.time }
                .filter { it.time <= Instant.now() }

            val calibrated = weatherService.calibrate(readingHistory, prefs)
            // TODO: Load observations directly
            observations = readingHistory.map { reading ->
                Reading(
                    WeatherObservation(
                        calibrated.firstOrNull { it.time == reading.time }?.value,
                        calibrateTemperature(reading.temperature),
                        reading.humidity,
                        null
                    ),
                    reading.time
                )
            }

            lifecycleScope.launch {
                updateForecast()
            }
        }

        cloudRepo.getAllLive().observe(viewLifecycleOwner) {
            cloudObservations = it.map {
                Reading(WeatherObservation(null, null, null, it.value.coverage), it.time)
            }
            update()
//            val cover = it.maxByOrNull { it.time }?.value?.coverage
//            binding.cloudCover.text = if (cover == null) {
//                "-"
//            } else {
//                formatService.formatPercentage(cover * 100) + "\n" + formatService.formatCloudCover(
//                    cloudService.classifyCloudCover(cover)
//                )
//            }
        }

        barometer.asLiveData().observe(viewLifecycleOwner, { update() })
        thermometer.asLiveData().observe(viewLifecycleOwner, { update() })
    }

    override fun onDestroy() {
        super.onDestroy()
        leftQuickAction?.onDestroy()
        rightQuickAction?.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        leftQuickAction?.onResume()
        rightQuickAction?.onResume()

        useSeaLevelPressure = prefs.weather.useSeaLevelPressure
        altitude = altimeter.altitude
        units = prefs.pressureUnits

        pressureSetpoint = prefs.weather.pressureSetpoint

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (!altimeter.hasValidReading) {
                    altimeter.read()
                }
            }
            withContext(Dispatchers.Main) {
                altitude = altimeter.altitude
                update()
            }
        }

        update()

        if (!prefs.weather.shouldMonitorWeather) {
            val error = UserError(
                USER_ERROR_WEATHER_MONITOR_OFF,
                getString(R.string.weather_monitoring_disabled),
                R.drawable.ic_weather,
                action = getString(R.string.enable)
            ) {
                prefs.weather.shouldMonitorWeather = true
                WeatherUpdateScheduler.start(requireContext())
                requireMainActivity().errorBanner.dismiss(USER_ERROR_WEATHER_MONITOR_OFF)
            }
            requireMainActivity().errorBanner.report(error)
        }
    }

    override fun onPause() {
        super.onPause()
        leftQuickAction?.onPause()
        rightQuickAction?.onPause()
        requireMainActivity().errorBanner.dismiss(USER_ERROR_WEATHER_MONITOR_OFF)
    }


    private fun update() {
        if (!isBound) return
        if (barometer.pressure == 0.0f) return

        if (throttle.isThrottled()) {
            return
        }

        val readings = getCalibratedPressures()

        displayChart(readings)

        val setpoint = getSetpoint()
        displayPressure()
        displayTemperature()
        displayClouds()

        if (setpoint != null && System.currentTimeMillis() - valueSelectedTime > 2000) {
            displaySetpoint(setpoint)
        } else if (System.currentTimeMillis() - valueSelectedTime > 2000) {
            binding.pressureMarker.text = ""
        }
    }

    private fun displaySetpoint(setpoint: PressureReading) {
        val formatted = formatService.formatPressure(
            Pressure(setpoint.value, PressureUnits.Hpa).convertTo(units),
            Units.getDecimalPlaces(units)
        )

        val timeAgo = Duration.between(setpoint.time, Instant.now())
        binding.pressureMarker.text = getString(
            R.string.pressure_setpoint_format,
            formatted,
            formatService.formatDuration(timeAgo, true)
        )
    }

    private fun getCalibratedPressures(includeCurrent: Boolean = false): List<PressureReading> {
        val readings = readingHistory.toMutableList()
        if (includeCurrent) {
            readings.add(
                PressureAltitudeReading(
                    Instant.now(),
                    barometer.pressure,
                    altimeter.altitude,
                    thermometer.temperature,
                    if (altimeter is IGPS) (altimeter as IGPS).verticalAccuracy else null
                )
            )
        }

        return weatherService.calibrate(readings, prefs)
    }

    private fun displayChart(readings: List<PressureReading>) {
        val displayReadings = readings.filter {
            Duration.between(
                it.time,
                Instant.now()
            ) <= prefs.weather.pressureHistory
        }

        if (displayReadings.size >= 2) {
            val totalTime = Duration.between(
                displayReadings.first().time, displayReadings.last().time
            )
            var hours = totalTime.toHours()
            val minutes = totalTime.toMinutes() % 60

            when (hours) {
                0L -> binding.pressureHistoryDuration.text = context?.resources?.getQuantityString(
                    R.plurals.last_minutes,
                    minutes.toInt(),
                    minutes
                )
                else -> {
                    if (minutes >= 30) hours++
                    binding.pressureHistoryDuration.text =
                        context?.resources?.getQuantityString(
                            R.plurals.last_hours,
                            hours.toInt(),
                            hours
                        )
                }
            }

        }

        if (displayReadings.isNotEmpty()) {
            chart.setUnits(units)

            val chartData = displayReadings.map {
                val timeAgo = Duration.between(Instant.now(), it.time).seconds / (60f * 60f)
                Pair(
                    timeAgo as Number,
                    (PressureUnitUtils.convert(
                        it.value,
                        units
                    )) as Number
                )
            }

            chart.plot(chartData)
        }
    }

    private suspend fun updateForecast() {
        val hourly = withContext(Dispatchers.IO) {
            weatherForecastService.getHourlyForecast()
        }

        val daily = withContext(Dispatchers.IO) {
            weatherForecastService.getDailyForecast()
        }

        withContext(Dispatchers.Main) {
            binding.forecastNow.text = formatService.formatShortTermWeather(
                hourly,
                prefs.weather.useRelativeWeatherPredictions
            )
            binding.forecastToday.text = getLongTermWeatherDescription(daily)
        }
    }

    private fun getSetpoint(): PressureReading? {
        val setpoint = pressureSetpoint
        return if (useSeaLevelPressure) {
            setpoint?.seaLevel(prefs.weather.seaLevelFactorInTemp)
        } else {
            setpoint?.pressureReading()
        }
    }

    private fun getCurrentTemperature(): Float {
        return calibrateTemperature(thermometer.temperature)
    }

    private fun calibrateTemperature(temperature: Float): Float {
        val calibrated1 = prefs.weather.minActualTemperature
        val uncalibrated1 = prefs.weather.minBatteryTemperature
        val calibrated2 = prefs.weather.maxActualTemperature
        val uncalibrated2 = prefs.weather.maxBatteryTemperature

        return calibrated1 + (calibrated2 - calibrated1) * (uncalibrated1 - temperature) / (uncalibrated1 - uncalibrated2)
    }

    private fun getCurrentPressure(): PressureReading {
        return if (useSeaLevelPressure) {
            val reading = PressureAltitudeReading(
                Instant.now(),
                barometer.pressure,
                altimeter.altitude,
                thermometer.temperature,
                if (altimeter is IGPS) (altimeter as IGPS).verticalAccuracy else null
            )
            weatherService.calibrate(readingHistory + listOf(reading), prefs).lastOrNull()
                ?: reading.seaLevel(prefs.weather.seaLevelFactorInTemp)
        } else {
            PressureReading(Instant.now(), barometer.pressure)
        }
    }

    private fun getCurrentObservation(): Reading<WeatherObservation> {
        return Reading(
            WeatherObservation(getCurrentPressure().value, getCurrentTemperature(), null, null),
            Instant.now()
        )
    }

    private fun getAllObservations(): List<Reading<WeatherObservation>> {
        return (observations + cloudObservations + listOf(getCurrentObservation())).sortedBy { it.time }
    }

    private fun displayTemperature() {
        val field = TemperatureObservationField(formatService, prefs.temperatureUnits)
        val observations = getAllObservations()
        binding.temperature.text = field.getTitle(observations)
    }

    private fun displayPressure() {
        val field = PressureObservationField(formatService, weatherService, requireContext(), units)
        val observations = getAllObservations()
        binding.pressure.text =
            field.getTitle(observations) + "\n" + field.getSubtitle(observations)
    }

    private fun displayClouds() {
        val field = CloudObservationField(formatService, cloudService)
        val observations = getAllObservations()
        binding.cloudCover.text =
            field.getTitle(observations) + "\n" + field.getSubtitle(observations)
    }

    private fun getLongTermWeatherDescription(weather: Weather): String {
        return when (weather) {
            Weather.ImprovingFast, Weather.ImprovingSlow -> getString(R.string.forecast_improving)
            Weather.WorseningSlow, Weather.WorseningFast, Weather.Storm -> getString(R.string.forecast_worsening)
            else -> ""
        }
    }

    // TODO: Get weather chart class for selected field

    private fun getQuickActionButton(
        type: QuickActionType,
        button: FloatingActionButton
    ): QuickActionButton {
        return when (type) {
            QuickActionType.Whistle -> QuickActionWhistle(button, this)
            QuickActionType.Flashlight -> QuickActionFlashlight(button, this)
            QuickActionType.Clouds -> QuickActionClouds(button, this)
            QuickActionType.Temperature -> QuickActionThermometer(button, this)
            QuickActionType.LowPowerMode -> LowPowerQuickAction(button, this)
            else -> QuickActionNone(button, this)
        }
    }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityWeatherBinding {
        return ActivityWeatherBinding.inflate(layoutInflater, container, false)
    }

}
