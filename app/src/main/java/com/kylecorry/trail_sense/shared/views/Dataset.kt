package com.kylecorry.trail_sense.shared.views

import androidx.annotation.ColorInt

data class Dataset(val points: List<Pair<Number, Number>>, @ColorInt val color: Int, val label: String = "")