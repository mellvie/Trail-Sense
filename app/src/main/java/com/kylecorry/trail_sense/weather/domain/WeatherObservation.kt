package com.kylecorry.trail_sense.weather.domain

data class WeatherObservation(
    val pressure: Float?,
    val temperature: Float?,
    val humidity: Float?,
    val cloudCover: Float?
)