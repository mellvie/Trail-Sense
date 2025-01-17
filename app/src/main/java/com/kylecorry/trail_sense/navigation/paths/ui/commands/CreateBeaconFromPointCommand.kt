package com.kylecorry.trail_sense.navigation.paths.ui.commands

import android.content.Context
import com.kylecorry.andromeda.core.system.GeoUri
import com.kylecorry.sol.math.SolMath.roundPlaces
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.navigation.paths.domain.Path
import com.kylecorry.trail_sense.navigation.paths.domain.PathPoint
import com.kylecorry.trail_sense.shared.AppUtils


class CreateBeaconFromPointCommand(private val context: Context) : IPathPointCommand {

    override fun execute(path: Path, point: PathPoint) {
        val params = mutableMapOf(
            "label" to (path.name ?: context.getString(R.string.waypoint))
        )

        if (point.elevation != null) {
            params["ele"] = point.elevation.roundPlaces(2).toString()
        }

        AppUtils.placeBeacon(
            context,
            GeoUri(point.coordinate, null, params)
        )
    }
}