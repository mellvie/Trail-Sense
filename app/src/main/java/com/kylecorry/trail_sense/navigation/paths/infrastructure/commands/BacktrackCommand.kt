package com.kylecorry.trail_sense.navigation.paths.infrastructure.commands

import android.content.Context
import com.kylecorry.trail_sense.navigation.paths.domain.PathPoint
import com.kylecorry.trail_sense.navigation.paths.infrastructure.persistence.PathService
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.commands.CoroutineCommand
import com.kylecorry.trail_sense.shared.extensions.onIO
import com.kylecorry.trail_sense.shared.networkQuality
import com.kylecorry.trail_sense.shared.sensors.NullCellSignalSensor
import com.kylecorry.trail_sense.shared.sensors.SensorService
import com.kylecorry.trail_sense.shared.sensors.altimeter.MedianAltimeter
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.Instant

class BacktrackCommand(private val context: Context, private val pathId: Long = 0) :
    CoroutineCommand {

    private val prefs = UserPreferences(context)

    private val sensorService = SensorService(context)
    private val gps = sensorService.getGPS()
    private val altimeter = MedianAltimeter(sensorService.getAltimeter())
    private val cellSignalSensor =
        if (prefs.backtrackSaveCellHistory && pathId == 0L) sensorService.getCellSignal() else NullCellSignalSensor()

    private val pathService = PathService.getInstance(context)

    override suspend fun execute() {
        updateSensors()
        val point = recordWaypoint()
        CreateLastSignalBeaconCommand(context).execute(point)
    }

    private suspend fun updateSensors() {
        onIO {
            try {
                withTimeoutOrNull(Duration.ofSeconds(10).toMillis()) {
                    val jobs = mutableListOf<Job>()
                    jobs.add(launch { gps.read() })

                    jobs.add(launch { altimeter.read() })

                    jobs.add(launch { cellSignalSensor.read() })

                    jobs.joinAll()
                }
            } finally {
                gps.stop(null)
                altimeter.stop(null)
                cellSignalSensor.stop(null)
            }
        }
    }


    private suspend fun recordWaypoint(): PathPoint {
        return onIO {
            val waypoint = PathPoint(
                0,
                pathId,
                gps.location,
                altimeter.altitude,
                Instant.now(),
                cellSignalSensor.networkQuality()
            )

            if (pathId == 0L) {
                pathService.addBacktrackPoint(waypoint)
            } else {
                pathService.addWaypoint(waypoint)
            }
            waypoint
        }
    }

}