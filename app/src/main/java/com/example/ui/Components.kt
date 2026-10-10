package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.NumberFormatter
import com.example.ui.theme.CyberClear
import com.example.ui.theme.CyberDigitButton
import com.example.ui.theme.CyberEquals
import com.example.ui.theme.CyberMemory
import com.example.ui.theme.CyberOnDigit
import com.example.ui.theme.CyberOnEquals
import com.example.ui.theme.CyberOperator
import com.example.ui.theme.CyberSci

enum class ButtonType {
    DIGIT,
    OPERATOR,
    EQUALS,
    CLEAR,
    MEMORY,
    SCI
}

@Composable
fun CalcButton(
    text: String,
    type: ButtonType,
    modifier: Modifier = Modifier,
    testTag: String = "btn_$text",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1f, label = "button_press_scale")

    val (bgColor, textColor) = when (type) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
        ButtonType.OPERATOR -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        ButtonType.EQUALS -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        ButtonType.CLEAR -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f) to MaterialTheme.colorScheme.error
        ButtonType.MEMORY -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) to MaterialTheme.colorScheme.tertiary
        ButtonType.SCI -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) to MaterialTheme.colorScheme.primary
    }

    val fontSize = when {
        text.length > 3 -> 15.sp
        text.length > 2 -> 18.sp
        type == ButtonType.EQUALS -> 26.sp
        else -> 22.sp
    }

    Box(
        modifier = modifier
            .scale(scale)
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = textColor),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = if (type == ButtonType.DIGIT) FontWeight.Medium else FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MemoryRibbon(
    memory: Double,
    onMc: () -> Unit,
    onMr: () -> Unit,
    onMPlus: () -> Unit,
    onMMinus: () -> Unit,
    onMs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasMemory = memory != 0.0
    val memoryLabel = if (hasMemory) "M: ${NumberFormatter.format(memory)}" else "M: 0"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasMemory) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = memoryLabel,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MemoryActionChip(label = "MC", testTag = "mem_mc", onClick = onMc, enabled = hasMemory)
            MemoryActionChip(label = "MR", testTag = "mem_mr", onClick = onMr, enabled = hasMemory)
            MemoryActionChip(label = "M+", testTag = "mem_m_plus", onClick = onMPlus)
            MemoryActionChip(label = "M−", testTag = "mem_m_minus", onClick = onMMinus)
            MemoryActionChip(label = "MS", testTag = "mem_ms", onClick = onMs)
        }
    }
}

@Composable
private fun MemoryActionChip(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CalculatorDisplay(
    expression: String,
    previewResult: String,
    isEvaluated: Boolean,
    isDeg: Boolean,
    errorMessage: String?,
    onToggleDeg: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Top row with DEG/RAD badge and Quick Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DEG / RAD toggle pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = onToggleDeg)
                        .testTag("deg_rad_toggle")
                ) {
                    Text(
                        text = if (isDeg) "DEG" else "RAD",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (expression.isNotEmpty() || previewResult.isNotEmpty()) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_display_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Копировать",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Expression Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    fontSize = if (isEvaluated) 38.sp else if (expression.length > 12) 30.sp else 38.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEvaluated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("expression_text")
                )
            }

            // Preview Result or Error Message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontSize = 15.sp,
                        color = if (errorMessage.contains("Память") || errorMessage.contains("Из памяти") || errorMessage.contains("Добавлено") || errorMessage.contains("Сохранено")) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("error_message_text")
                    )
                } else if (previewResult.isNotEmpty() && !isEvaluated) {
                    Text(
                        text = "= $previewResult",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("preview_result_text")
                    )
                }
            }
        }
    }
}
