package com.psspl.vinsdk

import java.util.Date

data class VinResult(
    val vin: String,
    val confidence: Float,
    val timestamp: Date
)
