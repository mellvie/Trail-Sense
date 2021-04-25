package com.kylecorry.trail_sense.navigation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.kylecorry.trail_sense.databinding.ActivityNavigatorBinding
import com.kylecorry.trail_sense.navigation.domain.NavigationService
import com.kylecorry.trail_sense.navigation.infrastructure.NavigationPreferences
import com.kylecorry.trail_sense.navigation.infrastructure.persistence.BeaconRepo
import com.kylecorry.trail_sense.shared.FormatServiceV2
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.roundPlaces
import com.kylecorry.trail_sense.shared.sensors.SensorService
import com.kylecorry.trailsensecore.domain.astronomy.moon.MoonTruePhase
import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.trailsensecore.domain.geo.Coordinate
import com.kylecorry.trailsensecore.domain.geo.Path
import com.kylecorry.trailsensecore.domain.navigation.Beacon
import com.kylecorry.trailsensecore.domain.units.Distance
import com.kylecorry.trailsensecore.domain.units.Quality
import com.kylecorry.trailsensecore.infrastructure.sensors.orientation.DeviceOrientation
import com.kylecorry.trailsensecore.infrastructure.sensors.asLiveData
import com.kylecorry.trailsensecore.infrastructure.view.BoundFragment

class NavigateFragment2 : BoundFragment<ActivityNavigatorBinding>() {


    // Injectables
    private val formatService by lazy { FormatServiceV2(requireContext()) }
    private val sensorService by lazy { SensorService(requireContext()) }
    private val prefs by lazy { UserPreferences(requireContext()) }
    private val beaconRepo by lazy { BeaconRepo.getInstance(requireContext()) }
    private val navigationService = NavigationService()

    // Sensors
    private val gps by lazy { sensorService.getGPS(false) }
    private val altimeter by lazy { sensorService.getAltimeter(false) }
    private val compass by lazy { sensorService.getCompass() }
    private val orientationSensor by lazy { sensorService.getDeviceOrientation() }
    private val speedometer by lazy { sensorService.getSpeedometer(prefSpeedometerMode == NavigationPreferences.SpeedometerMode.Instantaneous) }

    // Cached prefs
    private val prefCoordinateFormat by lazy { prefs.navigation.coordinateFormat }
    private val prefBaseUnits by lazy { prefs.baseDistanceUnits }
    private val prefDistanceUnits by lazy { prefs.distanceUnits }
    private val prefSpeedometerMode by lazy { prefs.navigation.speedometerMode }
    private val prefShowLinearCompass by lazy { prefs.navigation.showLinearCompass }
    private val prefShowRadarCompass by lazy { prefs.navigation.useRadarCompass }
    private val prefNearby by lazy { prefs.navigation.showMultipleBeacons }
    private val prefNearbyCount by lazy { prefs.navigation.numberOfVisibleBeacons }
    private val prefNearbyDistance by lazy { prefs.navigation.maxBeaconDistance }

    // Sensor readings
    private var location = Coordinate.zero
    private var altitude = 0f
    private var speed = 0f
    private var azimuth = 0f
    private var declination = 0f
    private var orientation = DeviceOrientation.Orientation.Flat

    // Accuracy readings
    private var gpsQuality = Quality.Unknown
    private var compassQuality = Quality.Unknown

    // UI
    private var compassType = CompassType.Round

    // Beacons, paths, and bearings
    private var beacons = listOf<Beacon>()
    private var nearby = listOf<Beacon>()
    private var paths = listOf<Path>()
    private var destination: Beacon? = null
    private var destinationBearing: Float? = null
    private var sunBearing = 0f
    private var moonBearing = 0f
    private var isSunUp = false
    private var isMoonUp = false
    private var moonPhase = MoonTruePhase.Full

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityNavigatorBinding {
        return ActivityNavigatorBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gps.asLiveData().observe(viewLifecycleOwner, { onLocationChange(gps.location) })
        altimeter.asLiveData().observe(viewLifecycleOwner, { onAltitudeChange(altimeter.altitude) })
        compass.asLiveData().observe(viewLifecycleOwner, { onCompassChange(compass.rawBearing) })
        speedometer.asLiveData()
            .observe(viewLifecycleOwner, { onSpeedChange(speedometer.speed.speed) })

        if (prefShowLinearCompass) {
            orientationSensor.asLiveData()
                .observe(viewLifecycleOwner, { onOrientationChange(orientationSensor.orientation) })
        }

        if (prefNearby && prefShowRadarCompass) {
            binding.roundCompass.isInvisible = true
            binding.radarCompass.isVisible = true
            compassType = CompassType.Radar
        } else {
            binding.roundCompass.isVisible = true
            binding.radarCompass.isInvisible = true
            compassType = CompassType.Round
        }

        if (prefNearby){
            beaconRepo.getBeacons().observe(viewLifecycleOwner, { onBeaconsChanged(it.map { it.toBeacon() }) })
        }

    }

    private fun onBeaconsChanged(beacons: List<Beacon>){
        this.beacons = beacons
        updateNearbyBeacons()
    }

    private fun onSpeedChange(speed: Float) {
        if (this.speed == speed.roundPlaces(SPEED_DECIMAL_PLACES)) {
            return
        }

        this.speed = speed.roundPlaces(SPEED_DECIMAL_PLACES)

        showSpeed(this.speed)
    }

    private fun onCompassChange(azimuth: Float) {
        if (this.azimuth == azimuth.roundPlaces(COMPASS_DECIMAL_PLACES)) {
            return
        }

        this.azimuth = azimuth.roundPlaces(COMPASS_DECIMAL_PLACES)

        showAzimuth(this.azimuth)

        updateCompass()
    }

    private fun onAltitudeChange(altitude: Float) {
        if (altitude.roundPlaces(1) == this.altitude) {
            return
        }

        this.altitude = altitude.roundPlaces(1)

        showAltitude(altitude)

        // If destination has altitude
        updateDestination()
    }

    private fun onLocationChange(newLocation: Coordinate) {
        if (areLocationsSame(location, newLocation, LOCATION_DECIMAL_PLACES)) {
            // Don't do a location update if nothing has changed
            return
        }

        location = newLocation
        val newDeclination = getDeclination(newLocation)
        if (declination != newDeclination.roundPlaces(COMPASS_DECIMAL_PLACES)) {
            declination = newDeclination.roundPlaces(COMPASS_DECIMAL_PLACES)
            compass.declination = declination
        }

        // TODO: Filter nearby beacons

        showLocation(newLocation)

        // TODO: Determine GPS accuracy

        // Recompute declination, if changed:
        updateCompass()

        updateNearbyBeacons()
    }

    private fun onOrientationChange(newOrientation: DeviceOrientation.Orientation) {
        if (orientation == newOrientation) {
            return
        }

        val requiresChange =
            orientation == DeviceOrientation.Orientation.Portrait || newOrientation == DeviceOrientation.Orientation.Portrait
        orientation = newOrientation

        if (!requiresChange) {
            return
        }

        val isPortrait = orientation == DeviceOrientation.Orientation.Portrait

        compassType = if (isPortrait && prefShowLinearCompass){
            CompassType.Linear
        } else if (prefNearby && prefShowRadarCompass){
            CompassType.Radar
        } else {
            CompassType.Round
        }
        binding.linearCompass.isGone = compassType != CompassType.Linear
        binding.radarCompass.isGone = compassType != CompassType.Radar
        binding.roundCompass.isInvisible = compassType != CompassType.Round

        updateCompass()
        // TODO: Add support for sighting camera
    }

    private fun updateNearbyBeacons(){
        if (!prefNearby){
            return
        }
        nearby = navigationService.getNearbyBeacons(location, beacons, prefNearbyCount, 0f, prefNearbyDistance).toList()
    }


    // Display

    private fun showAltitude(altitude: Float) {
        val altitudeDistance = Distance.meters(altitude).convertTo(prefBaseUnits)
        binding.altitude.text = formatService.formatDistance(altitudeDistance)
    }

    private fun showLocation(location: Coordinate) {
        binding.location.text = formatService.formatLocation(location, prefCoordinateFormat)
    }

    private fun showAzimuth(azimuth: Float) {
        binding.compassAzimuth.text = formatService.formatDegrees(azimuth, replace360 = true)
        binding.compassDirection.text = formatService.formatDirection(Bearing(azimuth).direction)
    }

    private fun showSpeed(speed: Float) {
        binding.speed.text = formatService.formatSpeed(speed, prefDistanceUnits)
    }

    private fun updateCompass() {
        when(compassType){
            CompassType.Linear -> {
                binding.linearCompass.setAzimuth(azimuth)
//                binding.linearCompass.setNearby(nearby)
            }
            CompassType.Round -> {
                binding.roundCompass.setAzimuth(azimuth)
            }
            CompassType.Radar -> {
                binding.radarCompass.setAzimuth(azimuth)
                binding.radarCompass.setPaths(paths)
                binding.radarCompass.setNearby(nearby)
//                binding.radarCompass.setSun()
//                binding.radarCompass.setMoon()
            }
        }
    }

    private fun updateDestination() {}


    // Helpers

    private fun getDeclination(location: Coordinate): Float {
        return 0f
    }

    private fun areLocationsSame(first: Coordinate, second: Coordinate, places: Int): Boolean {
        val firstLatitude = first.latitude.roundPlaces(places)
        val secondLatitude = second.latitude.roundPlaces(places)

        if (firstLatitude != secondLatitude) {
            return false
        }

        val firstLongitude = first.longitude.roundPlaces(places)
        val secondLongitude = second.longitude.roundPlaces(places)

        return firstLongitude == secondLongitude
    }


    companion object {
        private const val LOCATION_DECIMAL_PLACES = 6
        private const val COMPASS_DECIMAL_PLACES = 1
        private const val SPEED_DECIMAL_PLACES = 2
    }

    private enum class CompassType {
        Linear,
        Round,
        Radar
    }

}