package com.example

import com.example.engine.EvaluationResult
import com.example.engine.ExpressionEvaluator
import com.example.engine.NumberFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testArithmeticOperations() {
    val evaluator = ExpressionEvaluator(isDegMode = true)

    // Basic arithmetic
    val sum = evaluator.evaluate("12.5 + 7.5")
    assertEquals("20", (sum as EvaluationResult.Success).formatted)

    val sub = evaluator.evaluate("100 - 45")
    assertEquals("55", (sub as EvaluationResult.Success).formatted)

    val mul = evaluator.evaluate("6 * 7")
    assertEquals("42", (mul as EvaluationResult.Success).formatted)

    val div = evaluator.evaluate("144 / 12")
    assertEquals("12", (div as EvaluationResult.Success).formatted)
  }

  @Test
  fun testScientificOperations() {
    val evaluator = ExpressionEvaluator(isDegMode = true)

    // Trigonometry in DEG mode
    val sin30 = evaluator.evaluate("sin(30)")
    assertEquals("0.5", (sin30 as EvaluationResult.Success).formatted)

    val cos0 = evaluator.evaluate("cos(0)")
    assertEquals("1", (cos0 as EvaluationResult.Success).formatted)

    // Power and Square root
    val pow = evaluator.evaluate("2^8")
    assertEquals("256", (pow as EvaluationResult.Success).formatted)

    val sqrt = evaluator.evaluate("√(64)")
    assertEquals("8", (sqrt as EvaluationResult.Success).formatted)

    // Factorial
    val fact = evaluator.evaluate("5!")
    assertEquals("120", (fact as EvaluationResult.Success).formatted)
  }

  @Test
  fun testDivisionByZero() {
    val evaluator = ExpressionEvaluator(isDegMode = true)
    val result = evaluator.evaluate("50 / 0")
    assertTrue(result is EvaluationResult.Error)
    assertEquals("Деление на ноль!", (result as EvaluationResult.Error).message)
  }

  @Test
  fun testNumberFormatter() {
    assertEquals("42", NumberFormatter.format(42.0))
    assertEquals("3.14", NumberFormatter.format(3.14))
    assertEquals("0", NumberFormatter.format(0.0))
  }
}
