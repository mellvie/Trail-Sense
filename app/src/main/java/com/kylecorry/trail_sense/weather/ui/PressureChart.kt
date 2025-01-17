package com.kylecorry.trail_sense.weather.ui

import com.github.mikephil.charting.charts.LineChart
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.sol.math.SolMath.roundPlaces
import com.kylecorry.sol.units.Pressure
import com.kylecorry.sol.units.PressureUnits
import com.kylecorry.sol.units.Reading
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.views.SimpleLineChart
import java.time.Duration
import java.time.Instant


class PressureChart(
    chart: LineChart,
    private val selectionListener: ((timeAgo: Duration?, pressure: Float?) -> Unit)? = null
) {

    private val simpleChart = SimpleLineChart(chart, chart.context.getString(R.string.no_data))
    private var startTime = Instant.now()

    private var minRange = MIN_RANGE
    private var granularity = 1f

    private val color = Resources.getAndroidColorAttr(chart.context, R.attr.colorPrimary)

    init {
        simpleChart.configureYAxis(
            granularity = granularity,
            labelCount = 5,
            drawGridLines = true
        )

        simpleChart.configureXAxis(
            labelCount = 0,
            drawGridLines = false
        )

        setClickable(selectionListener != null)
    }

    private fun setClickable(clickable: Boolean) {
        if (!clickable) {
            simpleChart.setOnValueSelectedListener(null)
            return
        }
        simpleChart.setOnValueSelectedListener {
            if (it == null) {
                selectionListener?.invoke(null, null)
                return@setOnValueSelectedListener
            }
            val seconds = it.x * 60 * 60
            val duration = Duration.between(startTime.plusSeconds(seconds.toLong()), Instant.now())
            selectionListener?.invoke(duration, it.y)
        }
    }

    private fun setUnits(units: PressureUnits) {
        minRange = Pressure.hpa(MIN_RANGE).convertTo(units).pressure
        granularity = Pressure.hpa(1f).convertTo(units).pressure.roundPlaces(2)
    }

    fun plot(data: List<Reading<Pressure>>) {
        startTime = data.firstOrNull()?.time
        setUnits(data.firstOrNull()?.value?.units ?: PressureUnits.Hpa)
        val values = SimpleLineChart.getDataFromReadings(data, startTime) {
            it.pressure
        }

        val range = SimpleLineChart.getYRange(values, granularity, minRange)
        simpleChart.configureYAxis(
            minimum = range.start,
            maximum = range.end,
            granularity = granularity,
            labelCount = 5,
            drawGridLines = true
        )

        simpleChart.plot(values, color)
    }

    companion object {
        const val MIN_RANGE = 40f
    }
}