package com.kylecorry.trail_sense.weather.ui.observations

import com.kylecorry.sol.units.Reading
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.FormatService
import com.kylecorry.trail_sense.weather.domain.CloudService
import com.kylecorry.trail_sense.weather.domain.WeatherObservation

class CloudObservationField(
    private val formatService: FormatService,
    private val cloudService: CloudService
) : IWeatherObservationField {
    override fun getTitle(observations: List<Reading<WeatherObservation>>): String {
        val observation = getCloudCover(observations).lastOrNull()
        val cover = observation?.value ?: return "-"
        return formatService.formatPercentage(cover * 100)
    }

    override fun getSubtitle(observations: List<Reading<WeatherObservation>>): String? {
        val observation = getCloudCover(observations).lastOrNull()
        val cover = observation?.value ?: return null
        return formatService.formatCloudCover(cloudService.classifyCloudCover(cover))
    }

    override fun getIcon(observations: List<Reading<WeatherObservation>>): Int {
        return R.drawable.cloud
    }

    private fun getCloudCover(observations: List<Reading<WeatherObservation>>): List<Reading<Float>> {
        return observations.filter { it.value.cloudCover != null }
            .map { Reading(it.value.cloudCover!!, it.time) }
    }
}