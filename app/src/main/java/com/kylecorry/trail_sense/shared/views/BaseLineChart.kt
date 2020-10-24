package com.kylecorry.trail_sense.shared.views

import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kylecorry.trailsensecore.infrastructure.system.UiUtils

class BaseLineChart(private val chart: LineChart, private val noDataText: String = "") :
    IChartView {

    init {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.xAxis.setDrawLabels(false)
        chart.axisRight.setDrawLabels(false)
        val primaryColor = UiUtils.androidTextColorPrimary(chart.context)
        val r = primaryColor.red
        val g = primaryColor.green
        val b = primaryColor.blue
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.gridColor = Color.argb(50, r, g, b)
        chart.axisLeft.textColor = Color.argb(150, r, g, b)
        chart.axisRight.setDrawGridLines(false)
        chart.xAxis.setDrawAxisLine(false)
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisRight.setDrawAxisLine(false)
        chart.setNoDataText(noDataText)
    }

    override val x: Float
        get() = chart.x
    override val y: Float
        get() = chart.y
    override val width: Float
        get() = chart.width.toFloat()
    override val height: Float
        get() = chart.height.toFloat()

    override fun plot(datasets: List<Dataset>) {
        val lineDataSets = datasets.map { getLineDataSet(it) }
        val lineData = LineData(lineDataSets)
        chart.data = lineData
        chart.legend.isEnabled = false
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    override fun getPoint(datasetIdx: Int, entryIdx: Int): Pair<Float, Float> {
        val entry = chart.lineData.getDataSetByIndex(datasetIdx).getEntryForIndex(entryIdx)
        val point = chart.getPixelForValues(entry.x, entry.y, YAxis.AxisDependency.LEFT)
        return Pair(point.x.toFloat() + chart.x, point.y.toFloat() + chart.y)
    }

    override fun setOnPointClickListener(listener: OnPointClickListener) {
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                listener.onClick(null, null)
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e ?: return
                listener.onClick(h?.dataSetIndex, e.x to e.y)
            }
        })
    }

    private fun getLineDataSet(dataset: Dataset): LineDataSet {
        val set1 = LineDataSet(getEntries(dataset), dataset.label)
        set1.color = dataset.color
        set1.fillAlpha = 180
        set1.lineWidth = 3f
        set1.setDrawValues(false)
        set1.fillColor = dataset.color
        set1.setCircleColor(dataset.color)
        set1.setDrawCircleHole(false)
        set1.setDrawCircles(true)
        set1.circleRadius = 1.5f
        set1.setDrawFilled(false)
        return set1
    }

    private fun getEntries(dataset: Dataset): List<Entry> {
        return dataset.points.map { Entry(it.first.toFloat(), it.second.toFloat()) }
    }
}