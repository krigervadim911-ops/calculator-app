package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.converter.UnitConverterScreen
import com.example.ui.tip.TipCalculatorScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showHistorySheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✨ Калькулятор 3000",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // History Icon with Badge
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("top_bar_history_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (state.history.isNotEmpty()) {
                                    Badge {
                                        Text(state.history.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "История",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Haptic toggle
                    IconButton(
                        onClick = { viewModel.onToggleHaptic() },
                        modifier = Modifier.testTag("top_bar_haptic_btn")
                    ) {
                        Icon(
                            imageVector = if (state.hapticEnabled) Icons.Default.Vibration else Icons.Outlined.Vibration,
                            contentDescription = "Вибрация",
                            tint = if (state.hapticEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Theme toggle
                    IconButton(
                        onClick = {
                            val newTheme = if (state.themeMode == "CYBER") "LIGHT" else "CYBER"
                            viewModel.onSetTheme(newTheme)
                        },
                        modifier = Modifier.testTag("top_bar_theme_btn")
                    ) {
                        Icon(
                            imageVector = if (state.themeMode == "CYBER") Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Тема",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // About / Tribute
                    IconButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.testTag("top_bar_about_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "О приложении",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.onSelectTab(0) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                    label = { Text("Калькулятор") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.onSelectTab(1) },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                    label = { Text("Конвертер") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.onSelectTab(2) },
                    icon = { Icon(Icons.Default.Paid, contentDescription = null) },
                    label = { Text("Чаевые") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.selectedTab) {
                0 -> {
                    CalculatorMainContent(
                        state = state,
                        viewModel = viewModel,
                        onCopy = {
                            val textToCopy = if (state.isEvaluated) state.expression else state.previewResult.ifEmpty { state.expression }
                            if (textToCopy.isNotEmpty()) {
                                clipboard.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, "Скопировано: $textToCopy", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                1 -> {
                    UnitConverterScreen(
                        onVibrate = { viewModel.vibrate() }
                    )
                }
                2 -> {
                    TipCalculatorScreen(
                        onVibrate = { viewModel.vibrate() }
                    )
                }
            }
        }
    }

    if (showHistorySheet) {
        HistoryBottomSheet(
            history = state.history,
            onDismiss = { showHistorySheet = false },
            onUseExpression = { expr -> viewModel.onUseHistoryExpression(expr) },
            onUseResult = { res -> viewModel.onUseHistoryResult(res) },
            onCopy = { text ->
                clipboard.setText(AnnotatedString(text))
                Toast.makeText(context, "Скопировано: $text", Toast.LENGTH_SHORT).show()
            },
            onDeleteItem = { item -> viewModel.onDeleteHistoryItem(item) },
            onClearHistory = { viewModel.onClearHistory() }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun CalculatorMainContent(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Section
        CalculatorDisplay(
            expression = state.expression,
            previewResult = state.previewResult,
            isEvaluated = state.isEvaluated,
            isDeg = state.isDeg,
            errorMessage = state.errorMessage,
            onToggleDeg = { viewModel.onToggleDegRad() },
            onCopy = onCopy
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Memory Ribbon & Sci Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemoryRibbon(
                memory = state.memory,
                onMc = { viewModel.onMemoryClear() },
                onMr = { viewModel.onMemoryRecall() },
                onMPlus = { viewModel.onMemoryAdd() },
                onMMinus = { viewModel.onMemorySubtract() },
                onMs = { viewModel.onMemoryStore() },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Sci Button Toggle
            IconButton(
                onClick = { viewModel.onToggleScientific() },
                modifier = Modifier.testTag("sci_toggle_btn")
            ) {
                Icon(
                    imageVector = if (state.isScientificExpanded) Icons.Default.Science else Icons.Outlined.Science,
                    contentDescription = "Инженерный режим",
                    tint = if (state.isScientificExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scientific Panel (Expandable)
        AnimatedVisibility(
            visible = state.isScientificExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: sin, cos, tan, ln, log
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcButton("sin", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onFunction("sin") }
                    CalcButton("cos", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onFunction("cos") }
                    CalcButton("tan", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onFunction("tan") }
                    CalcButton("ln", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onFunction("ln") }
                    CalcButton("log", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onFunction("log") }
                }

                // Row 2: √, x², x^y, π, e, !
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcButton("√", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onSquareRoot() }
                    CalcButton("x²", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onSquare() }
                    CalcButton("xʸ", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onPower() }
                    CalcButton("π", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onConstant("π") }
                    CalcButton("e", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onConstant("e") }
                    CalcButton("x!", ButtonType.SCI, Modifier.weight(1f)) { viewModel.onFactorial() }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Standard Keypad Rows
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: AC, ( ), %, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton("AC", ButtonType.CLEAR, Modifier.weight(1f), testTag = "btn_ac") { viewModel.onClearAll() }
                CalcButton("( )", ButtonType.OPERATOR, Modifier.weight(1f), testTag = "btn_parens") { viewModel.onParenthesis() }
                CalcButton("%", ButtonType.OPERATOR, Modifier.weight(1f), testTag = "btn_percent") { viewModel.onPercentage() }
                CalcButton("÷", ButtonType.OPERATOR, Modifier.weight(1f), testTag = "btn_divide") { viewModel.onOperator("÷") }
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton("7", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_7") { viewModel.onDigit("7") }
                CalcButton("8", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_8") { viewModel.onDigit("8") }
                CalcButton("9", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_9") { viewModel.onDigit("9") }
                CalcButton("×", ButtonType.OPERATOR, Modifier.weight(1f), testTag = "btn_multiply") { viewModel.onOperator("×") }
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton("4", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_4") { viewModel.onDigit("4") }
                CalcButton("5", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_5") { viewModel.onDigit("5") }
                CalcButton("6", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_6") { viewModel.onDigit("6") }
                CalcButton("−", ButtonType.OPERATOR, Modifier.weight(1f), testTag = "btn_minus") { viewModel.onOperator("−") }
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton("1", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_1") { viewModel.onDigit("1") }
                CalcButton("2", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_2") { viewModel.onDigit("2") }
                CalcButton("3", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_3") { viewModel.onDigit("3") }
                CalcButton("+", ButtonType.OPERATOR, Modifier.weight(1f), testTag = "btn_plus") { viewModel.onOperator("+") }
            }

            // Row 5: ⌫ or ±, 0, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton("⌫", ButtonType.CLEAR, Modifier.weight(1f), testTag = "btn_backspace") { viewModel.onBackspace() }
                CalcButton("0", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_0") { viewModel.onDigit("0") }
                CalcButton(".", ButtonType.DIGIT, Modifier.weight(1f), testTag = "btn_dot") { viewModel.onDecimal() }
                CalcButton("=", ButtonType.EQUALS, Modifier.weight(1f), testTag = "btn_equals") { viewModel.onCalculate() }
            }
        }
    }
}
