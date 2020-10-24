package com.kylecorry.trail_sense.shared.views

interface OnPointClickListener {
    fun onClick(datasetIndex: Int?, point: Pair<Number, Number>?)
}