package com.example.engine

import kotlin.math.PI
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

sealed class EvaluationResult {
    data class Success(val value: Double, val formatted: String) : EvaluationResult()
    data class Error(val message: String) : EvaluationResult()
}

class ExpressionEvaluator(var isDegMode: Boolean = true) {

    fun evaluate(expression: String): EvaluationResult {
        val sanitized = preprocess(expression)
        if (sanitized.isBlank()) {
            return EvaluationResult.Error("Пустое выражение")
        }

        return try {
            val parser = Parser(sanitized, isDegMode)
            val result = parser.parse()
            if (result.isNaN()) {
                EvaluationResult.Error("Неопределенный результат")
            } else if (result.isInfinite()) {
                EvaluationResult.Error("Деление на ноль!")
            } else {
                EvaluationResult.Success(result, NumberFormatter.format(result))
            }
        } catch (e: DivisionByZeroException) {
            EvaluationResult.Error("Деление на ноль!")
        } catch (e: MathException) {
            EvaluationResult.Error(e.message ?: "Ошибка вычисления")
        } catch (_: Exception) {
            EvaluationResult.Error("Неверный формат")
        }
    }

    private fun preprocess(expr: String): String {
        var s = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(",", ".")
            .replace(" ", "")

        // Replace constants
        s = s.replace("π", PI.toString())
        s = s.replace("e", E.toString())
        s = s.replace("√", "sqrt")

        // Auto-close missing parentheses
        val openCount = s.count { it == '(' }
        val closeCount = s.count { it == ')' }
        if (openCount > closeCount) {
            s += ")".repeat(openCount - closeCount)
        }

        return s
    }

    private class DivisionByZeroException : RuntimeException("Деление на ноль!")
    private class MathException(msg: String) : RuntimeException(msg)

    private class Parser(private val input: String, private val isDeg: Boolean) {
        private var pos = 0
        private val length = input.length

        fun parse(): Double {
            val res = parseExpression()
            if (pos < length) {
                throw MathException("Некорректный символ: '${input[pos]}'")
            }
            return res
        }

        private fun peek(): Char? = if (pos < length) input[pos] else null

        private fun advance(): Char {
            val c = input[pos]
            pos++
            return c
        }

        private fun match(expected: Char): Boolean {
            if (peek() == expected) {
                pos++
                return true
            }
            return false
        }

        // Expression: Term (('+' | '-') Term)*
        private fun parseExpression(): Double {
            var left = parseTerm()
            while (true) {
                when {
                    match('+') -> {
                        val right = parseTerm()
                        left += right
                    }
                    match('-') -> {
                        val right = parseTerm()
                        left -= right
                    }
                    else -> break
                }
            }
            return left
        }

        // Term: Factor (('*' | '/' | '%') Factor)*
        private fun parseTerm(): Double {
            var left = parseFactor()
            while (true) {
                when {
                    match('*') -> {
                        val right = parseFactor()
                        left *= right
                    }
                    match('/') -> {
                        val right = parseFactor()
                        if (abs(right) < 1e-15) throw DivisionByZeroException()
                        left /= right
                    }
                    match('%') -> {
                        // If standard modulus or percentage
                        val right = parseFactor()
                        if (abs(right) < 1e-15) throw DivisionByZeroException()
                        left %= right
                    }
                    else -> break
                }
            }
            return left
        }

        // Factor: Power ('^' Power)*
        private fun parseFactor(): Double {
            var base = parseUnary()
            while (match('^')) {
                val exponent = parseUnary()
                base = base.pow(exponent)
            }
            return base
        }

        // Unary: ('+' | '-')? Primary ('!')? ('%')?
        private fun parseUnary(): Double {
            if (match('+')) {
                return parseUnary()
            }
            if (match('-')) {
                return -parseUnary()
            }

            var value = parsePrimary()

            // Postfix operators: ! and %
            while (true) {
                when {
                    match('!') -> {
                        value = factorial(value)
                    }
                    match('%') -> {
                        value /= 100.0
                    }
                    else -> break
                }
            }

            return value
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != kotlin.math.floor(n) || n > 170) {
                throw MathException("Факториал определен для n ∈ [0, 170]")
            }
            var acc = 1.0
            val limit = n.toLong()
            for (i in 2..limit) {
                acc *= i.toDouble()
            }
            return acc
        }

        private fun parsePrimary(): Double {
            val c = peek() ?: throw MathException("Ожидается число")

            // Parentheses
            if (match('(')) {
                val res = parseExpression()
                if (!match(')')) {
                    throw MathException("Ожидается ')'")
                }
                return res
            }

            // Functions or identifiers
            if (c.isLetter()) {
                val ident = StringBuilder()
                while (peek()?.isLetter() == true) {
                    ident.append(advance())
                }
                val fnName = ident.toString().lowercase()

                if (!match('(')) {
                    throw MathException("Ожидается '(' после функции $fnName")
                }
                val arg = parseExpression()
                if (!match(')')) {
                    throw MathException("Ожидается ')' после аргумента $fnName")
                }

                return applyFunction(fnName, arg)
            }

            // Numbers
            if (c.isDigit() || c == '.') {
                val numStr = StringBuilder()
                var hasDot = false
                while (pos < length) {
                    val ch = input[pos]
                    if (ch.isDigit()) {
                        numStr.append(ch)
                        pos++
                    } else if (ch == '.' && !hasDot) {
                        hasDot = true
                        numStr.append(ch)
                        pos++
                    } else {
                        break
                    }
                }
                return numStr.toString().toDoubleOrNull() ?: throw MathException("Неверное число")
            }

            throw MathException("Неожиданный символ: '$c'")
        }

        private fun applyFunction(name: String, arg: Double): Double {
            val toRadians = if (isDeg) Math.toRadians(arg) else arg

            return when (name) {
                "sin" -> sin(toRadians)
                "cos" -> cos(toRadians)
                "tan" -> {
                    if (isDeg && (abs(arg % 180) == 90.0)) {
                        throw MathException("tan($arg°) не существует")
                    }
                    tan(toRadians)
                }
                "asin" -> {
                    if (arg < -1.0 || arg > 1.0) throw MathException("asin вне [-1, 1]")
                    val rad = asin(arg)
                    if (isDeg) Math.toDegrees(rad) else rad
                }
                "acos" -> {
                    if (arg < -1.0 || arg > 1.0) throw MathException("acos вне [-1, 1]")
                    val rad = acos(arg)
                    if (isDeg) Math.toDegrees(rad) else rad
                }
                "atan" -> {
                    val rad = atan(arg)
                    if (isDeg) Math.toDegrees(rad) else rad
                }
                "sqrt" -> {
                    if (arg < 0) throw MathException("Квадратный корень из отриц. числа")
                    sqrt(arg)
                }
                "ln" -> {
                    if (arg <= 0) throw MathException("ln определен только для > 0")
                    ln(arg)
                }
                "log" -> {
                    if (arg <= 0) throw MathException("log определен только для > 0")
                    log10(arg)
                }
                "abs" -> abs(arg)
                else -> throw MathException("Неизвестная функция: $name")
            }
        }
    }
}
