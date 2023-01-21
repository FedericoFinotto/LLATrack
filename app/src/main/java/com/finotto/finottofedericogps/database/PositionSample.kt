package com.finotto.finottofedericogps.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class PositionSample(
        val altitudine: Double,
        val longitudine: Double,
        val latitudine: Double,
        val timestamp: Date
) : Parcelable