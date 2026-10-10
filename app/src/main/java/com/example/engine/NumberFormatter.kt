package com.example.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatter {

    fun format(value: Double): String {
        if (value.isNaN()) return "Не число"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        // Check if value is very close to an integer
        val rounded = Math.round(value)
        if (Math.abs(value - rounded) < 1e-12) {
            return rounded.toString()
        }

        // Check for scientific notation threshold
        if (Math.abs(value) >= 1e12 || (Math.abs(value) < 1e-6 && Math.abs(value) > 0)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            return df.format(value)
        }

        // Clean decimal formatting (avoiding 0.1 + 0.2 = 0.30000000000000004)
        val bd = try {
            BigDecimal(value, MathContext(12, RoundingMode.HALF_UP)).stripTrailingZeros()
        } catch (_: Exception) {
            BigDecimal.valueOf(value)
        }

        return bd.toPlainString()
    }

    fun formatDisplay(rawText: String): String {
        // Formats expression for display with nice spacing and operator symbols
        return rawText
            .replace("*", " × ")
            .replace("/", " ÷ ")
            .replace("+", " + ")
            .replace("-", " − ")
            .replace("  ", " ")
    }
}
