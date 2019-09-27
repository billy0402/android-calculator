package edu.ntub.calculator

import android.widget.TextView
import java.text.DecimalFormat

fun String.isNumber(): Boolean {
    return all {
        (it.isDigit() || it == '.' || it == 'E')
    }
}

fun String.pointFormatter(): String {
    val dotIndex = indexOf('.')
    val eIndex = indexOf('E')
    val pointString = substring(dotIndex, eIndex)
    if (pointString.length > 3) {
        val newPoint = DecimalFormat("#.##").format(pointString.toDouble())
        return replace(pointString, newPoint.substring(1))
    }

    return this
}

fun ArrayList<Symbol>.indexes(element1: Symbol, element2: Symbol): List<Int> {
    return mapIndexedNotNull { index, symbol ->
        if (symbol == element1 || symbol == element2) index else null
    }
}

fun TextView.getMaxTextLength(): Int {
    val wordWidth = this.paint.measureText("0")
    val screenWidth = resources.displayMetrics.widthPixels

    return (screenWidth / wordWidth).toInt()
}
