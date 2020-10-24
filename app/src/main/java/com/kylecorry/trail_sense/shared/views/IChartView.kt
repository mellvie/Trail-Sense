package com.kylecorry.trail_sense.shared.views

interface IChartView {
    val x: Float
    val y: Float
    val width: Float
    val height: Float
    fun plot(datasets: List<Dataset>)
    fun getPoint(datasetIdx: Int, entryIdx: Int): Pair<Float, Float>
    fun setOnPointClickListener(listener: OnPointClickListener)
}