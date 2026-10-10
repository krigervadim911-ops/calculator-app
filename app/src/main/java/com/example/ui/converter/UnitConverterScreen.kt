package com.example.ui.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

enum class UnitCategory(val title: String) {
    LENGTH("Длина"),
    WEIGHT("Масса"),
    TEMPERATURE("Температура"),
    SPEED("Скорость"),
    AREA("Площадь"),
    DATA("Данные")
}

data class UnitItem(val name: String, val factorToBase: Double, val offset: Double = 0.0)

val UnitCategories = mapOf(
    UnitCategory.LENGTH to listOf(
        UnitItem("Метры (м)", 1.0),
        UnitItem("Километры (км)", 1000.0),
        UnitItem("Сантиметры (см)", 0.01),
        UnitItem("Миллиметры (мм)", 0.001),
        UnitItem("Футы (ft)", 0.3048),
        UnitItem("Дюймы (in)", 0.0254),
        UnitItem("Мили (mi)", 1609.344)
    ),
    UnitCategory.WEIGHT to listOf(
        UnitItem("Килограммы (кг)", 1.0),
        UnitItem("Граммы (г)", 0.001),
        UnitItem("Миллиграммы (мг)", 0.000001),
        UnitItem("Фунты (lb)", 0.45359237),
        UnitItem("Унции (oz)", 0.0283495),
        UnitItem("Тонны (т)", 1000.0)
    ),
    UnitCategory.TEMPERATURE to listOf(
        UnitItem("Цельсий (°C)", 1.0),
        UnitItem("Фаренгейт (°F)", 1.0),
        UnitItem("Кельвин (K)", 1.0)
    ),
    UnitCategory.SPEED to listOf(
        UnitItem("км/ч", 1.0),
        UnitItem("м/с", 3.6),
        UnitItem("миль/ч (mph)", 1.60934),
        UnitItem("узлы (kn)", 1.852)
    ),
    UnitCategory.AREA to listOf(
        UnitItem("кв. метры (м²)", 1.0),
        UnitItem("кв. километры (км²)", 1000000.0),
        UnitItem("Гектары (га)", 10000.0),
        UnitItem("Сотки", 100.0),
        UnitItem("кв. футы (ft²)", 0.092903)
    ),
    UnitCategory.DATA to listOf(
        UnitItem("Мегабайты (МБ)", 1.0),
        UnitItem("Гигабайты (ГБ)", 1024.0),
        UnitItem("Терабайты (ТБ)", 1048576.0),
        UnitItem("Килобайты (КБ)", 1.0 / 1024.0),
        UnitItem("Байты (Б)", 1.0 / 1048576.0)
    )
)

@Composable
fun UnitConverterScreen(
    onVibrate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    val units = UnitCategories[selectedCategory] ?: emptyList()

    var fromUnit by remember(selectedCategory) { mutableStateOf(units[0]) }
    var toUnit by remember(selectedCategory) { mutableStateOf(units.getOrElse(1) { units[0] }) }

    var inputString by remember { mutableStateOf("1") }

    val convertedValue = remember(inputString, fromUnit, toUnit, selectedCategory) {
        val input = inputString.toDoubleOrNull() ?: 0.0
        if (selectedCategory == UnitCategory.TEMPERATURE) {
            convertTemperature(input, fromUnit.name, toUnit.name)
        } else {
            val inBase = input * fromUnit.factorToBase
            inBase / toUnit.factorToBase
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Category Selector Chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitCategory.values().forEach { cat ->
                    FilterChip(
                        selected = cat == selectedCategory,
                        onClick = {
                            selectedCategory = cat
                            onVibrate()
                        },
                        label = { Text(cat.title, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Conversion Cards
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    UnitSelectorRow(
                        label = "Из",
                        unitName = fromUnit.name,
                        valueText = if (inputString.isEmpty()) "0" else inputString,
                        availableUnits = units,
                        onSelectUnit = { fromUnit = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                val temp = fromUnit
                                fromUnit = toUnit
                                toUnit = temp
                                onVibrate()
                            },
                            modifier = Modifier.testTag("swap_units_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Поменять местами",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    UnitSelectorRow(
                        label = "В",
                        unitName = toUnit.name,
                        valueText = NumberFormatter.format(convertedValue),
                        availableUnits = units,
                        onSelectUnit = { toUnit = it },
                        isTarget = true
                    )
                }
            }
        }

        // Mini keypad for converter
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = listOf(
                listOf("7", "8", "9", "AC"),
                listOf("4", "5", "6", "⌫"),
                listOf("1", "2", "3", "±"),
                listOf("0", "00", ".", "✓")
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
                        CalcButton(
                            text = key,
                            type = btnType,
                            modifier = Modifier.weight(1f),
                            testTag = "conv_btn_$key",
                            onClick = {
                                onVibrate()
                                when (key) {
                                    "AC" -> inputString = "0"
                                    "⌫" -> {
                                        inputString = if (inputString.length <= 1) "0" else inputString.dropLast(1)
                                    }
                                    "." -> {
                                        if (!inputString.contains(".")) {
                                            inputString = "$inputString."
                                        }
                                    }
                                    "±" -> {
                                        inputString = if (inputString.startsWith("-")) {
                                            inputString.removePrefix("-")
                                        } else if (inputString != "0") {
                                            "-$inputString"
                                        } else inputString
                                    }
                                    "00" -> {
                                        if (inputString != "0") inputString += "00"
                                    }
                                    "✓" -> {
                                        // Done
                                    }
                                    else -> {
                                        inputString = if (inputString == "0") key else inputString + key
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

@Composable
private fun UnitSelectorRow(
    label: String,
    unitName: String,
    valueText: String,
    availableUnits: List<UnitItem>,
    onSelectUnit: (UnitItem) -> Unit,
    isTarget: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = unitName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableUnits.forEach { u ->
                    DropdownMenuItem(
                        text = { Text(u.name) },
                        onClick = {
                            onSelectUnit(u)
                            expanded = false
                        }
                    )
                }
            }
        }

        Text(
            text = valueText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun convertTemperature(value: Double, from: String, to: String): Double {
    val celsius = when {
        from.contains("°C") -> value
        from.contains("°F") -> (value - 32.0) * 5.0 / 9.0
        else -> value - 273.15 // Kelvin
    }

    return when {
        to.contains("°C") -> celsius
        to.contains("°F") -> (celsius * 9.0 / 5.0) + 32.0
        else -> celsius + 273.15 // Kelvin
    }
}
