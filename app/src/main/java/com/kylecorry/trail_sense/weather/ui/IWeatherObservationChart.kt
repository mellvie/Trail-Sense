package com.kylecorry.trail_sense.weather.ui

import com.kylecorry.sol.units.Reading
import com.kylecorry.trail_sense.weather.domain.WeatherObservation

interface IWeatherObservationChart {

    fun plot(readings: List<Reading<WeatherObservation>>)

}