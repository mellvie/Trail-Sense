package com.kylecorry.trail_sense.weather.ui.observations

import com.kylecorry.sol.units.Reading
import com.kylecorry.sol.units.Temperature
import com.kylecorry.sol.units.TemperatureUnits
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.FormatService
import com.kylecorry.trail_sense.weather.domain.WeatherObservation

class TemperatureObservationField(
    private val formatService: FormatService,
    private val units: TemperatureUnits
) : IWeatherObservationField {
    override fun getTitle(observations: List<Reading<WeatherObservation>>): String {
        val observation = getTemperatures(observations).lastOrNull()
        val temperature = observation?.value ?: return "-"
        val converted = Temperature.celsius(temperature).convertTo(units)
        return formatService.formatTemperature(converted)
    }

    override fun getSubtitle(observations: List<Reading<WeatherObservation>>): String? {
        return null
    }

    override fun getIcon(observations: List<Reading<WeatherObservation>>): Int {
        return R.drawable.thermometer
    }

    private fun getTemperatures(observations: List<Reading<WeatherObservation>>): List<Reading<Float>> {
        return observations.filter { it.value.temperature != null }
            .map { Reading(it.value.temperature!!, it.time) }
    }
}