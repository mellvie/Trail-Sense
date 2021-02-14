package com.kylecorry.trail_sense.weather.domain.sealevel

import android.hardware.SensorManager
import com.kylecorry.trail_sense.weather.domain.AltitudeReading
import com.kylecorry.trailsensecore.domain.weather.PressureAltitudeReading
import java.time.Duration
import kotlin.math.abs

internal class GroupAltitudeConverter() : IAltitudeCalculator {
    override fun convert(
        readings: List<PressureAltitudeReading>,
        interpolateAltitudeChanges: Boolean,
        errors: List<Float?>
    ): List<AltitudeReading> {

        if (readings.size <= 1) {
            return readings.map { AltitudeReading(it.time, it.altitude) }
        }

        val groups = mutableListOf<MutableList<Pair<AltitudeReading, Float>>>()
        for (i in readings.indices) {
            val reading = readings[i]
            val error = errors.getOrNull(i) ?: 50f
            if (groups.isEmpty()) {
                groups.add(mutableListOf(AltitudeReading(reading.time, reading.altitude) to error))
                continue
            }
            val lastGroup = groups.last()
            val lastAltitude = lastGroup.last().first
            val lastError = lastGroup.last().second

            if (couldBeSame(reading.altitude, error, lastAltitude.value, lastError)){
                lastGroup.add(AltitudeReading(reading.time, reading.altitude) to error)
            } else {
                val kf = kalman(lastGroup.map { it.first.value to it.second })
                val lastSlp = PressureAltitudeReading(reading.time, readings[i - 1].pressure, kf.first, 0f).seaLevel()
                val estimatedAltitude = SensorManager.getAltitude(lastSlp.value, reading.pressure)
                if (couldBeSame(estimatedAltitude, error, reading.altitude, error)){
                    groups.add(mutableListOf(AltitudeReading(reading.time, estimatedAltitude) to error))
                } else {
                    lastGroup.add(AltitudeReading(reading.time, estimatedAltitude) to error)
                }
            }
        }

        val newAltitudes = mutableListOf<AltitudeReading>()
        for (group in groups) {
            val kf = kalman(group.map { it.first.value to it.second })
            for (i in group.indices){
                newAltitudes.add(AltitudeReading(group[i].first.time, kf.first))
            }
        }

        return newAltitudes
    }

    private fun couldBeSame(value1: Float, error1: Float, value2: Float, error2: Float): Boolean {
        val v1Min = value1 - error1
        val v1Max = value1 + error1
        val v2Min = value2 - error2
        val v2Max = value2 + error2
        return if (v2Min < v1Max && v2Max > v1Min) {
            true
        } else {
            v1Min < v2Max && v1Max > v2Min
        }
    }

    private fun kalman(values: List<Pair<Float, Float>>, processError: Float = 0f): Pair<Float, Float> {
        var lastEstimate = values.first().first
        var lastError = values.first().second

        for (value in values){
            lastError += processError
            val kg = lastError / (lastError + value.second)
            val estimate = lastEstimate + kg * (value.first - lastEstimate)
            lastError *= (1 - kg)
            lastEstimate = estimate
        }

        return lastEstimate to lastError
    }

    private fun nextValid(
        readings: List<AltitudeReading>,
        start: Int,
        fallback: AltitudeReading
    ): AltitudeReading {
        for (i in start until readings.size) {
            if (!readings[i].value.isNaN()) {
                return AltitudeReading(readings[i].time, readings[i].value)
            }
        }
        return fallback
    }

    private fun prevValid(
        readings: List<AltitudeReading>,
        start: Int,
        fallback: AltitudeReading
    ): AltitudeReading {
        for (i in start downTo 0) {
            if (!readings[i].value.isNaN()) {
                return AltitudeReading(readings[i].time, readings[i].value)
            }
        }
        return fallback
    }
}