package com.kylecorry.trail_sense.weather.ui.observations

import android.content.Context
import com.kylecorry.sol.science.meteorology.PressureCharacteristic
import com.kylecorry.sol.science.meteorology.PressureTendency
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.PressureUnits
import com.kylecorry.sol.units.Reading
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.FormatService
import com.kylecorry.trail_sense.shared.Units
import com.kylecorry.trail_sense.weather.domain.PressureReading
import com.kylecorry.trail_sense.weather.domain.WeatherObservation
import com.kylecorry.trail_sense.weather.domain.WeatherService

class PressureObservationField(
    private val formatService: FormatService,
    private val weatherService: WeatherService,
    private val context: Context,
    private val units: PressureUnits
) : IWeatherObservationField {
    override fun getTitle(observations: List<Reading<WeatherObservation>>): String {
        val observation = getPressures(observations).lastOrNull()
        val pressure = observation?.value ?: return "-"
        val converted = Pressure.hpa(pressure).convertTo(units)
        return formatService.formatPressure(converted, Units.getDecimalPlaces(units))
    }

    override fun getSubtitle(observations: List<Reading<WeatherObservation>>): String {
        val tendency = getTendency(getPressures(observations))
        val converted = Pressure.hpa(tendency.amount).convertTo(units)
        return context.getString(
            R.string.pressure_tendency_format_2,
            formatService.formatPressure(converted, Units.getDecimalPlaces(units) + 1)
        )
    }

    override fun getIcon(observations: List<Reading<WeatherObservation>>): Int {
        val tendency = getTendency(getPressures(observations))
        return when (tendency.characteristic) {
            PressureCharacteristic.Falling, PressureCharacteristic.FallingFast -> {
                R.drawable.ic_arrow_down
            }
            PressureCharacteristic.Rising, PressureCharacteristic.RisingFast -> {
                R.drawable.ic_arrow_up
            }
            else -> {
                R.drawable.steady
            }
        }
    }

    private fun getPressures(observations: List<Reading<WeatherObservation>>): List<Reading<Float>> {
        return observations.filter { it.value.pressure != null }
            .map { Reading(it.value.pressure!!, it.time) }
    }

    private fun getTendency(observations: List<Reading<Float>>): PressureTendency {
        return weatherService.getTendency(observations.map { PressureReading(it.time, it.value) })
    }
}