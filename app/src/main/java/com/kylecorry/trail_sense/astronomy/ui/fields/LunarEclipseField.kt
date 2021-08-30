package com.kylecorry.trail_sense.astronomy.ui.fields

import android.content.Context
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.astronomy.domain.LunarEclipse
import com.kylecorry.trail_sense.shared.FormatService

class LunarEclipseField(val eclipse: LunarEclipse, val showStart: Boolean) : AstroFieldTemplate() {
    override fun getTitle(context: Context): String {
        return if (showStart) {
            context.getString(R.string.lunar_eclipse_start)
        } else {
            context.getString(
                R.string.lunar_eclipse_end
            )
        }
    }

    override fun getValue(context: Context): String {
        return if (showStart) {
            getStartValue(context, eclipse)
        } else {
            getEndValue(context, eclipse)
        }
    }

    override fun getImage(context: Context): Int {
        return if (eclipse.isTotal) {
            R.drawable.ic_moon_total_eclipse
        } else {
            R.drawable.ic_moon_partial_eclipse
        }
    }


    private fun getStartValue(context: Context, eclipse: LunarEclipse): String {
        val formatService = FormatService(context)
        val eclipseAmount =
            if (eclipse.isTotal) context.getString(R.string.total) else context.getString(
                R.string.partial,
                formatService.formatPercentage(eclipse.magnitude * 100)
            )
        val time = formatService.formatTime(eclipse.start.toLocalTime(), includeSeconds = false)
        return "$time\n$eclipseAmount"
    }

    private fun getEndValue(context: Context, eclipse: LunarEclipse): String {
        val formatService = FormatService(context)
        val eclipseAmount =
            if (eclipse.isTotal) context.getString(R.string.total) else context.getString(
                R.string.partial,
                formatService.formatPercentage(eclipse.magnitude * 100)
            )
        val time = formatService.formatTime(eclipse.end.toLocalTime(), includeSeconds = false)
        return "$time\n$eclipseAmount"
    }
}