package com.example

data class TelemetryData(
    val isRaceOn: Boolean = false,
    val timestampMs: Long = 0,
    val engineMaxRpm: Float = 0f,
    val engineIdleRpm: Float = 0f,
    val currentEngineRpm: Float = 0f,
    val speedKmh: Float = 0f,
    val gear: Int = 0
)
