package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.EvaluationResult
import com.example.engine.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Калькулятор 3000", appName)
  }

  @Test
  fun `evaluate math expressions correctly`() {
    val evaluator = ExpressionEvaluator(isDegMode = true)

    val res1 = evaluator.evaluate("2 + 2 * 2")
    assertTrue(res1 is EvaluationResult.Success)
    assertEquals("6", (res1 as EvaluationResult.Success).formatted)

    val res2 = evaluator.evaluate("(10 + 20) / 2")
    assertTrue(res2 is EvaluationResult.Success)
    assertEquals("15", (res2 as EvaluationResult.Success).formatted)

    val res3 = evaluator.evaluate("10 / 0")
    assertTrue(res3 is EvaluationResult.Error)
    assertEquals("Деление на ноль!", (res3 as EvaluationResult.Error).message)
  }
}
