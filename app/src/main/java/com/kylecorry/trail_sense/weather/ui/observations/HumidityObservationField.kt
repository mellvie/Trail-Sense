package com.kylecorry.trail_sense.weather.ui.observations

import com.kylecorry.sol.units.Reading
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.FormatService
import com.kylecorry.trail_sense.weather.domain.WeatherObservation

class HumidityObservationField(
    private val formatService: FormatService
) : IWeatherObservationField {
    override fun getTitle(observations: List<Reading<WeatherObservation>>): String {
        val observation = getHumidity(observations).lastOrNull()
        val humidity = observation?.value ?: return "-"
        return formatService.formatPercentage(humidity)
    }

    override fun getSubtitle(observations: List<Reading<WeatherObservation>>): String? {
        return null
    }

    override fun getIcon(observations: List<Reading<WeatherObservation>>): Int {
        return R.drawable.ic_category_water
    }

    private fun getHumidity(observations: List<Reading<WeatherObservation>>): List<Reading<Float>> {
        return observations.filter { it.value.humidity != null }
            .map { Reading(it.value.humidity!!, it.time) }
    }
}