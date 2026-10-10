package com.example.ui.tip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.NumberFormatter
import com.example.ui.ButtonType
import com.example.ui.CalcButton

@Composable
fun TipCalculatorScreen(
    onVibrate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var billText by remember { mutableStateOf("1000") }
    var tipPercent by remember { mutableFloatStateOf(10f) }
    var peopleCount by remember { mutableIntStateOf(2) }

    val bill = billText.toDoubleOrNull() ?: 0.0
    val tipAmount = bill * (tipPercent / 100.0)
    val totalAmount = bill + tipAmount
    val perPersonAmount = if (peopleCount > 0) totalAmount / peopleCount else totalAmount
    val tipPerPerson = if (peopleCount > 0) tipAmount / peopleCount else tipAmount

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Per person summary card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "С каждого человека",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${NumberFormatter.format(perPersonAmount)} ₽",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Счет", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("${NumberFormatter.format(bill)} ₽", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Чаевые ($tipPercent%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("${NumberFormatter.format(tipAmount)} ₽", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Итого", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("${NumberFormatter.format(totalAmount)} ₽", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tip Percentage presets
            Text(
                text = "Процент чаевых: ${tipPercent.toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0f, 5f, 10f, 15f, 20f).forEach { pct ->
                    FilterChip(
                        selected = tipPercent == pct,
                        onClick = {
                            tipPercent = pct
                            onVibrate()
                        },
                        label = { Text("${pct.toInt()}%", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // People count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Персон: $peopleCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (peopleCount > 1) {
                                peopleCount--
                                onVibrate()
                            }
                        },
                        enabled = peopleCount > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Минус")
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = peopleCount.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (peopleCount < 50) {
                                peopleCount++
                                onVibrate()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Плюс")
                    }
                }
            }
        }

        // Bill numeric keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = listOf(
                listOf("7", "8", "9", "AC"),
                listOf("4", "5", "6", "⌫"),
                listOf("1", "2", "3", "00"),
                listOf("0", ".", "✓")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        val btnType = when (key) {
                            "AC", "⌫" -> ButtonType.CLEAR
                            "✓" -> ButtonType.EQUALS
                            else -> ButtonType.DIGIT
                        }
                        val weight = if (key == "✓") 2f else 1f
                        CalcButton(
                            text = key,
                            type = btnType,
                            modifier = Modifier.weight(weight),
                            testTag = "tip_btn_$key",
                            onClick = {
                                onVibrate()
                                when (key) {
                                    "AC" -> billText = "0"
                                    "⌫" -> {
                                        billText = if (billText.length <= 1) "0" else billText.dropLast(1)
                                    }
                                    "." -> {
                                        if (!billText.contains(".")) billText = "$billText."
                                    }
                                    "00" -> {
                                        if (billText != "0") billText += "00"
                                    }
                                    "✓" -> {}
                                    else -> {
                                        billText = if (billText == "0") key else billText + key
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
