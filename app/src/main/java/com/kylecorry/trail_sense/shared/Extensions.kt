package com.kylecorry.trail_sense.shared

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kylecorry.andromeda.core.system.GeoUri
import com.kylecorry.andromeda.core.units.PixelCoordinate
import com.kylecorry.andromeda.location.IGPS
import com.kylecorry.andromeda.signal.CellNetworkQuality
import com.kylecorry.andromeda.signal.ICellSignalSensor
import com.kylecorry.sol.math.SolMath.roundPlaces
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.units.DistanceUnits
import com.kylecorry.sol.units.Speed
import com.kylecorry.sol.units.TimeUnits
import com.kylecorry.trail_sense.main.MainActivity
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.navigation.beacons.domain.Beacon
import com.kylecorry.trail_sense.navigation.paths.domain.PathPoint
import com.kylecorry.trail_sense.shared.database.Identifiable

fun Fragment.requireMainActivity(): MainActivity {
    return requireActivity() as MainActivity
}

fun Fragment.requireBottomNavigation(): BottomNavigationView {
    return requireActivity().findViewById(R.id.bottom_navigation)
}

fun IGPS.getPathPoint(pathId: Long): PathPoint {
    return PathPoint(
        -1,
        pathId,
        location,
        altitude,
        time
    )
}

fun PixelCoordinate.toVector2(): Vector2 {
    return Vector2(x, y)
}

fun Vector2.toPixel(): PixelCoordinate {
    return PixelCoordinate(x, y)
}

fun <T : Identifiable> Array<T>.withId(id: Long): T? {
    return firstOrNull { it.id == id }
}

fun <T : Identifiable> Collection<T>.withId(id: Long): T? {
    return firstOrNull { it.id == id }
}

fun SeekBar.setOnProgressChangeListener(listener: (progress: Int, fromUser: Boolean) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            listener(progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }

    })
}

fun GeoUri.Companion.from(beacon: Beacon): GeoUri {
    val params = mutableMapOf(
        "label" to beacon.name
    )
    if (beacon.elevation != null) {
        params["ele"] = beacon.elevation.roundPlaces(2).toString()
    }

    return GeoUri(beacon.coordinate, null, params)
}

fun ImageButton.flatten() {
    backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    elevation = 0f
}

val ZERO_SPEED = Speed(0f, DistanceUnits.Meters, TimeUnits.Seconds)

fun ICellSignalSensor.networkQuality(): CellNetworkQuality? {
    val signal = signals.maxByOrNull { it.strength }
    return signal?.let { CellNetworkQuality(it.network, it.quality) }
}