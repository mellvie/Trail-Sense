package com.kylecorry.trail_sense.weather.domain.sealevel.dwell

import com.kylecorry.trailsensecore.domain.weather.PressureAltitudeReading
import com.kylecorry.trailsensecore.domain.weather.PressureReading

internal class AltimeterSeaLevelPressureConverter(
    private val altitudeCalculator: IAltitudeCalculator,
    private val useTemperature: Boolean
) {

    fun convert(
        readings: List<PressureAltitudeReading>,
        interpolateAltitudeChanges: Boolean
    ): List<PressureReading> {
        val altitudeHistory = altitudeCalculator.convert(readings, interpolateAltitudeChanges)

        return readings
            .mapIndexed { index, reading -> reading.copy(altitude = altitudeHistory[index].value) }
            .filterNot { it.altitude.isNaN() }
            .map { it.seaLevel(useTemperature) }
    }

}