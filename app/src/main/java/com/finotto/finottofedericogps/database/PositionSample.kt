package com.finotto.finottofedericogps.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Classe che memorizza i dati di un campionamento.
 */
@Parcelize
data class PositionSample(
        val altitudine: Double,
        val longitudine: Double,
        val latitudine: Double,
        val timestamp: Date
) : Parcelable