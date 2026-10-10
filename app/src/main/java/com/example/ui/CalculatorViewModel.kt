package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculatorPreferences
import com.example.engine.EvaluationResult
import com.example.engine.ExpressionEvaluator
import com.example.engine.NumberFormatter
import com.example.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val previewResult: String = "",
    val isEvaluated: Boolean = false,
    val memory: Double = 0.0,
    val isDeg: Boolean = true,
    val isScientificExpanded: Boolean = false,
    val history: List<HistoryItem> = emptyList(),
    val errorMessage: String? = null,
    val hapticEnabled: Boolean = true,
    val themeMode: String = "CYBER",
    val selectedTab: Int = 0 // 0: Calc, 1: Converter, 2: Tip
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = CalculatorPreferences(application)
    private val evaluator = ExpressionEvaluator(isDegMode = prefs.isDeg)

    private val _uiState = MutableStateFlow(
        CalculatorUiState(
            memory = prefs.memory,
            isDeg = prefs.isDeg,
            history = prefs.loadHistory(),
            hapticEnabled = prefs.isHapticEnabled,
            themeMode = prefs.themeMode
        )
    )
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrate() {
        if (!_uiState.value.hapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
            }
        } catch (_: Exception) {}
    }

    fun onDigit(digit: String) {
        vibrate()
        _uiState.update { state ->
            val newExpr = if (state.isEvaluated) {
                digit
            } else {
                state.expression + digit
            }
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                errorMessage = null,
                previewResult = calculatePreview(newExpr, state.isDeg)
            )
        }
    }

    fun onDecimal() {
        vibrate()
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val lastToken = getLastNumberToken(expr)
            if (lastToken.contains(".")) {
                state
            } else {
                val toAdd = if (expr.isEmpty() || isOperatorOrParen(expr.last())) "0." else "."
                val newExpr = expr + toAdd
                state.copy(
                    expression = newExpr,
                    isEvaluated = false,
                    previewResult = calculatePreview(newExpr, state.isDeg)
                )
            }
        }
    }

    fun onOperator(op: String) {
        vibrate()
        _uiState.update { state ->
            val expr = state.expression
            if (expr.isEmpty()) {
                if (op == "−") {
                    state.copy(expression = "−", isEvaluated = false)
                } else {
                    state
                }
            } else {
                val lastChar = expr.last()
                val newExpr = if (isOperator(lastChar)) {
                    expr.dropLast(1) + op
                } else {
                    expr + op
                }
                state.copy(
                    expression = newExpr,
                    isEvaluated = false,
                    previewResult = calculatePreview(newExpr, state.isDeg)
                )
            }
        }
    }

    fun onFunction(fn: String) {
        vibrate()
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val newExpr = "$expr$fn("
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                previewResult = calculatePreview(newExpr, state.isDeg)
            )
        }
    }

    fun onConstant(constant: String) {
        vibrate()
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val needsMul = expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')
            val prefix = if (needsMul) "×" else ""
            val newExpr = "$expr$prefix$constant"
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                previewResult = calculatePreview(newExpr, state.isDeg)
            )
        }
    }

    fun onParenthesis() {
        vibrate()
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }

            val canClose = openCount > closeCount && expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')' || expr.last() == 'π' || expr.last() == 'e' || expr.last() == '!')
            val toAdd = if (canClose) ")" else "("
            val newExpr = expr + toAdd

            state.copy(
                expression = newExpr,
                isEvaluated = false,
                previewResult = calculatePreview(newExpr, state.isDeg)
            )
        }
    }

    fun onFactorial() {
        vibrate()
        _uiState.update { state ->
            val expr = state.expression
            if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) {
                val newExpr = "$expr!"
                state.copy(
                    expression = newExpr,
                    isEvaluated = false,
                    previewResult = calculatePreview(newExpr, state.isDeg)
                )
            } else {
                state
            }
        }
    }

    fun onSquare() {
        vibrate()
        _uiState.update { state ->
            val expr = state.expression
            if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) {
                val newExpr = "$expr^2"
                state.copy(
                    expression = newExpr,
                    isEvaluated = false,
                    previewResult = calculatePreview(newExpr, state.isDeg)
                )
            } else {
                state
            }
        }
    }

    fun onPower() {
        onOperator("^")
    }

    fun onSquareRoot() {
        vibrate()
        _uiState.update { state ->
            val expr = if (state.isEvaluated) "" else state.expression
            val newExpr = "$expr√("
            state.copy(
                expression = newExpr,
                isEvaluated = false,
                previewResult = calculatePreview(newExpr, state.isDeg)
            )
        }
    }

    fun onPercentage() {
        vibrate()
        _uiState.update { state ->
            val expr = state.expression
            if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')')) {
                val newExpr = "$expr%"
                state.copy(
                    expression = newExpr,
                    isEvaluated = false,
                    previewResult = calculatePreview(newExpr, state.isDeg)
                )
            } else {
                state
            }
        }
    }

    fun onToggleSign() {
        vibrate()
        _uiState.update { state ->
            val expr = state.expression
            if (expr.isEmpty()) return@update state

            // Find last number or token
            val lastToken = getLastNumberToken(expr)
            if (lastToken.isNotEmpty()) {
                val startIdx = expr.length - lastToken.length
                val prefix = expr.substring(0, startIdx)

                val newExpr = if (prefix.endsWith("−")) {
                    prefix.dropLast(1) + lastToken
                } else if (prefix.endsWith("(-")) {
                    prefix.dropLast(2) + lastToken
                } else {
                    prefix + "−" + lastToken
                }

                state.copy(
                    expression = newExpr,
                    previewResult = calculatePreview(newExpr, state.isDeg)
                )
            } else {
                state
            }
        }
    }

    fun onBackspace() {
        vibrate()
        _uiState.update { state ->
            if (state.isEvaluated) {
                state.copy(expression = "", previewResult = "", isEvaluated = false)
            } else if (state.expression.isNotEmpty()) {
                // Check if ending with a function like "sin(", "cos(", "sqrt("
                var expr = state.expression
                val functions = listOf("sin(", "cos(", "tan(", "asin(", "acos(", "atan(", "sqrt(", "√(", "ln(", "log(")
                val matchedFn = functions.firstOrNull { expr.endsWith(it) }

                expr = if (matchedFn != null) {
                    expr.dropLast(matchedFn.length)
                } else {
                    expr.dropLast(1)
                }

                state.copy(
                    expression = expr,
                    errorMessage = null,
                    previewResult = calculatePreview(expr, state.isDeg)
                )
            } else {
                state
            }
        }
    }

    fun onClearAll() {
        vibrate()
        _uiState.update {
            it.copy(
                expression = "",
                previewResult = "",
                isEvaluated = false,
                errorMessage = null
            )
        }
    }

    fun onCalculate() {
        vibrate()
        val current = _uiState.value
        if (current.expression.isBlank()) return

        evaluator.isDegMode = current.isDeg
        when (val result = evaluator.evaluate(current.expression)) {
            is EvaluationResult.Success -> {
                val formatted = result.formatted
                val historyItem = HistoryItem(
                    expression = current.expression,
                    result = formatted
                )
                val newHistory = listOf(historyItem) + current.history
                prefs.saveHistory(newHistory)

                _uiState.update {
                    it.copy(
                        expression = formatted,
                        previewResult = "",
                        isEvaluated = true,
                        history = newHistory,
                        errorMessage = null
                    )
                }
            }
            is EvaluationResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    // --- Memory Operations ---

    fun onMemoryClear() {
        vibrate()
        prefs.memory = 0.0
        _uiState.update {
            it.copy(memory = 0.0, errorMessage = "Память очищена")
        }
    }

    fun onMemoryRecall() {
        vibrate()
        val mem = _uiState.value.memory
        val formatted = NumberFormatter.format(mem)
        _uiState.update { state ->
            val expr = if (state.isEvaluated) formatted else state.expression + formatted
            state.copy(
                expression = expr,
                isEvaluated = false,
                errorMessage = "Из памяти извлечено: $formatted",
                previewResult = calculatePreview(expr, state.isDeg)
            )
        }
    }

    fun onMemoryAdd() {
        vibrate()
        val currentVal = getCurrentActiveValue()
        if (currentVal != null) {
            val newMem = _uiState.value.memory + currentVal
            prefs.memory = newMem
            _uiState.update {
                it.copy(
                    memory = newMem,
                    errorMessage = "Добавлено в память (${NumberFormatter.format(newMem)})"
                )
            }
        }
    }

    fun onMemorySubtract() {
        vibrate()
        val currentVal = getCurrentActiveValue()
        if (currentVal != null) {
            val newMem = _uiState.value.memory - currentVal
            prefs.memory = newMem
            _uiState.update {
                it.copy(
                    memory = newMem,
                    errorMessage = "Вычтено из памяти (${NumberFormatter.format(newMem)})"
                )
            }
        }
    }

    fun onMemoryStore() {
        vibrate()
        val currentVal = getCurrentActiveValue()
        if (currentVal != null) {
            prefs.memory = currentVal
            _uiState.update {
                it.copy(
                    memory = currentVal,
                    errorMessage = "Сохранено в память: ${NumberFormatter.format(currentVal)}"
                )
            }
        }
    }

    private fun getCurrentActiveValue(): Double? {
        val current = _uiState.value
        // If evaluated, the expression is the value
        if (current.isEvaluated) {
            return current.expression.toDoubleOrNull()
        }
        // Try preview result
        if (current.previewResult.isNotBlank()) {
            return current.previewResult.toDoubleOrNull()
        }
        // Try evaluating full expression
        evaluator.isDegMode = current.isDeg
        return when (val res = evaluator.evaluate(current.expression)) {
            is EvaluationResult.Success -> res.value
            is EvaluationResult.Error -> null
        }
    }

    // --- History Operations ---

    fun onClearHistory() {
        vibrate()
        prefs.saveHistory(emptyList())
        _uiState.update { it.copy(history = emptyList(), errorMessage = "История очищена") }
    }

    fun onDeleteHistoryItem(item: HistoryItem) {
        vibrate()
        val updated = _uiState.value.history.filter { it.id != item.id }
        prefs.saveHistory(updated)
        _uiState.update { it.copy(history = updated) }
    }

    fun onUseHistoryExpression(expr: String) {
        vibrate()
        _uiState.update {
            it.copy(
                expression = expr,
                isEvaluated = false,
                previewResult = calculatePreview(expr, it.isDeg)
            )
        }
    }

    fun onUseHistoryResult(result: String) {
        vibrate()
        _uiState.update {
            it.copy(
                expression = result,
                isEvaluated = true,
                previewResult = ""
            )
        }
    }

    // --- Settings / Toggles ---

    fun onToggleDegRad() {
        vibrate()
        val newDeg = !_uiState.value.isDeg
        prefs.isDeg = newDeg
        _uiState.update { state ->
            state.copy(
                isDeg = newDeg,
                previewResult = calculatePreview(state.expression, newDeg)
            )
        }
    }

    fun onToggleScientific() {
        vibrate()
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun onToggleHaptic() {
        val newHaptic = !_uiState.value.hapticEnabled
        prefs.isHapticEnabled = newHaptic
        _uiState.update { it.copy(hapticEnabled = newHaptic) }
        if (newHaptic) vibrate()
    }

    fun onSetTheme(theme: String) {
        vibrate()
        prefs.themeMode = theme
        _uiState.update { it.copy(themeMode = theme) }
    }

    fun onSelectTab(index: Int) {
        vibrate()
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun calculatePreview(expr: String, isDeg: Boolean): String {
        if (expr.isBlank() || isOperator(expr.last())) return ""
        evaluator.isDegMode = isDeg
        return when (val res = evaluator.evaluate(expr)) {
            is EvaluationResult.Success -> res.formatted
            is EvaluationResult.Error -> ""
        }
    }

    private fun getLastNumberToken(expr: String): String {
        var idx = expr.length - 1
        val sb = StringBuilder()
        while (idx >= 0) {
            val c = expr[idx]
            if (c.isDigit() || c == '.') {
                sb.append(c)
                idx--
            } else {
                break
            }
        }
        return sb.reverse().toString()
    }

    private fun isOperator(c: Char): Boolean = c == '+' || c == '−' || c == '×' || c == '÷' || c == '^' || c == '%'

    private fun isOperatorOrParen(c: Char): Boolean = isOperator(c) || c == '(' || c == ')'
}
