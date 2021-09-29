package com.kylecorry.trail_sense.weather.ui.observations

interface IWeatherObservationFactory {

    fun getChart(type: WeatherObservationType): IWeatherObservationChart?

    fun getField(type: WeatherObservationType): IWeatherObservationField

}