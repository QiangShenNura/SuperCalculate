package com.stapler.supercalculate

import androidx.compose.ui.graphics.Color

data class ElementData(val symbol: String, val textColor: Color, val backgroundColor: Color)

enum class Status{
    INPUT,
    RESULT
}

data class CalculateState(
    val status: Status = Status.INPUT,
    val input: String = "",
    var result: String = ""
)