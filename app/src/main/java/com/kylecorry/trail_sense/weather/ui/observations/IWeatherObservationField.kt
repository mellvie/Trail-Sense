package com.kylecorry.trail_sense.weather.ui.observations

import androidx.annotation.DrawableRes
import com.kylecorry.sol.units.Reading
import com.kylecorry.trail_sense.weather.domain.WeatherObservation

interface IWeatherObservationField {
    fun getTitle(observations: List<Reading<WeatherObservation>>): String
    fun getSubtitle(observations: List<Reading<WeatherObservation>>): String?

    @DrawableRes
    fun getIcon(observations: List<Reading<WeatherObservation>>): Int
}