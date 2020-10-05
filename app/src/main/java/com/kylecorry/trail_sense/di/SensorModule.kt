package com.kylecorry.trail_sense.di

import android.content.Context
import com.kylecorry.trail_sense.navigation.infrastructure.flashlight.FlashlightHandler
import com.kylecorry.trail_sense.navigation.infrastructure.flashlight.IFlashlightHandler
import com.kylecorry.trail_sense.shared.sensors.DeviceOrientation
import com.kylecorry.trail_sense.shared.sensors.IOrientationSensor
import com.kylecorry.trail_sense.shared.sensors.SensorService
import com.kylecorry.trailsensecore.infrastructure.flashlight.Flashlight
import com.kylecorry.trailsensecore.infrastructure.sensors.altimeter.IAltimeter
import com.kylecorry.trailsensecore.infrastructure.sensors.barometer.IBarometer
import com.kylecorry.trailsensecore.infrastructure.sensors.compass.ICompass
import com.kylecorry.trailsensecore.infrastructure.sensors.declination.IDeclinationProvider
import com.kylecorry.trailsensecore.infrastructure.sensors.gps.IGPS
import com.kylecorry.trailsensecore.infrastructure.sensors.hygrometer.IHygrometer
import com.kylecorry.trailsensecore.infrastructure.sensors.inclinometer.IInclinometer
import com.kylecorry.trailsensecore.infrastructure.sensors.temperature.IThermometer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class SensorModule {

    @Singleton
    @Provides
    fun provideSensorService(@ApplicationContext context: Context): SensorService {
        return SensorService(context)
    }

    @Provides
    fun provideCompass(sensorService: SensorService): ICompass {
        return sensorService.getCompass()
    }

    @Provides
    fun provideGPS(sensorService: SensorService): IGPS {
        return sensorService.getGPS(false)
    }

    @Provides
    fun provideDeviceOrientation(sensorService: SensorService): DeviceOrientation {
        return sensorService.getDeviceOrientation()
    }

    @Provides
    fun provideAltimeter(sensorService: SensorService): IAltimeter {
        return sensorService.getAltimeter(false)
    }

    @Provides
    fun provideDeclination(sensorService: SensorService): IDeclinationProvider {
        return sensorService.getDeclinationProvider()
    }

    @Provides
    fun provideInclinometer(sensorService: SensorService): IInclinometer {
        return sensorService.getInclinometer()
    }

    @Provides
    fun provideOrientation(sensorService: SensorService): IOrientationSensor {
        return sensorService.getOrientationSensor()
    }

    @Provides
    fun provideThermometer(sensorService: SensorService): IThermometer {
        return sensorService.getThermometer()
    }

    @Provides
    fun provideBarometer(sensorService: SensorService): IBarometer {
        return sensorService.getBarometer()
    }

    @Provides
    fun provideHygrometer(sensorService: SensorService): IHygrometer {
        return sensorService.getHygrometer()
    }

    @Singleton
    @Provides
    fun provideFlashlight(@ApplicationContext context: Context): IFlashlightHandler {
        return FlashlightHandler(context)
    }

}