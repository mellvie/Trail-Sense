package com.kylecorry.trail_sense.weather.ui.observations

import com.kylecorry.sol.units.Reading
import com.kylecorry.trail_sense.weather.domain.WeatherObservation

interface IWeatherObservationChart {

    fun setup()

    fun plot(readings: List<Reading<WeatherObservation>>)

}