package com.example.unitconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unitconverter.ui.theme.UnitConverterTheme
import kotlin.math.roundToInt

// --- DATA STRUCTURES FOR ORGANIZATION ---
enum class ConversionCategory {
    LENGTH,
    MASS,
    TEMPERATURE,
    SPEED
}

data class UnitData(val name: String, val factor: Double)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnitConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContainer()
                }
            }
        }
    }
}

@Composable
fun AppContainer() {
    var selectedCategory by remember { mutableStateOf(ConversionCategory.LENGTH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Unit Converter",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        ScrollableRowCategorySelector(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(24.dp))
        UnitConverter(category = selectedCategory)
    }
}

@Composable
fun ScrollableRowCategorySelector(
    selectedCategory: ConversionCategory,
    onCategorySelected: (ConversionCategory) -> Unit
) {
    val allCategories = ConversionCategory.entries

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allCategories.forEach { category ->
            val isSelected = selectedCategory == category

            FilledTonalButton(
                onClick = { onCategorySelected(category) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    category.name.lowercase().replaceFirstChar { it.titlecase() },
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun UnitConverter(category: ConversionCategory) {
    val unitsList: List<UnitData> = remember(category) {
        when (category) {
            ConversionCategory.LENGTH -> listOf(
                UnitData("Meters", 1.0),
                UnitData("Kilometers", 1000.0),
                UnitData("CentiMeters", 0.01),
                UnitData("MilliMeters", 0.001),
                UnitData("Feet", 0.3048),
                UnitData("Miles", 1609.34),
            )
            ConversionCategory.MASS -> listOf(
                UnitData("Kilograms", 1.0),
                UnitData("Grams", 0.001),
                UnitData("Pounds", 0.453592),
                UnitData("Ounces", 0.0283495),
            )
            ConversionCategory.TEMPERATURE -> listOf(
                UnitData("Celsius", 1.0),
                UnitData("Fahrenheit", 1.0),
                UnitData("Kelvin", 1.0),
            )
            ConversionCategory.SPEED -> listOf(
                UnitData("Meters/sec", 1.0),
                UnitData("Kilometers/hr", 0.277778),
                UnitData("Miles/hr", 0.44704),
            )
        }
    }

    var inputValue by remember { mutableStateOf("") }
    var outputValue by remember { mutableStateOf("") }
    var inputUnitData by remember(category) { mutableStateOf(unitsList.first()) }
    var outputUnitData by remember(category) { mutableStateOf(unitsList.first()) }
    var iExpanded by remember { mutableStateOf(false) }
    var oExpanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }


    fun convertTemperature(input: Double, fromUnit: String, toUnit: String): Double {
        val celsius = when (fromUnit) {
            "Fahrenheit" -> (input - 32) * 5 / 9
            "Kelvin" -> input - 273.15
            else -> input
        }
        return when (toUnit) {
            "Fahrenheit" -> (celsius * 9 / 5) + 32
            "Kelvin" -> celsius + 273.15
            else -> celsius
        }
    }

    val convertUnits: () -> Unit = {
        val inputValueDouble = inputValue.toDoubleOrNull() ?: 0.0
        val result = if (category == ConversionCategory.TEMPERATURE) {
            convertTemperature(
                input = inputValueDouble,
                fromUnit = inputUnitData.name,
                toUnit = outputUnitData.name
            )
        } else {
            inputValueDouble * inputUnitData.factor / outputUnitData.factor
        }

        val roundedResult = (result * 1000.0).roundToInt() / 1000.0
        outputValue = roundedResult.toString()
    }

    LaunchedEffect(inputValue, inputUnitData, outputUnitData) { convertUnits() }

    LaunchedEffect(category) {
        inputValue = ""
        outputValue = ""
        focusRequester.requestFocus()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = inputValue,
            onValueChange = {
                inputValue = it.filter { char -> char.isDigit() || char == '.' }
            },
            label = { Text("Enter Value") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropdownSelector(
                    title = "From:",
                    selectedUnit = inputUnitData,
                    expanded = iExpanded,
                    onExpand = { iExpanded = it },
                    units = unitsList,
                    onUnitSelected = { newUnit ->
                        inputUnitData = newUnit
                        iExpanded = false
                    },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val tempUnit = inputUnitData
                        inputUnitData = outputUnitData
                        outputUnitData = tempUnit
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "Swap Units",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownSelector(
                    title = "To:",
                    selectedUnit = outputUnitData,
                    expanded = oExpanded,
                    onExpand = { oExpanded = it },
                    units = unitsList,
                    onUnitSelected = { newUnit ->
                        outputUnitData = newUnit
                        oExpanded = false
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Result:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    outputValue,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    outputUnitData.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DropdownSelector(
    title: String,
    selectedUnit: UnitData,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    units: List<UnitData>,
    onUnitSelected: (UnitData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpand(true) }
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    selectedUnit.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpand(false) }
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.name) },
                        onClick = {
                            onUnitSelected(unit)
                            onExpand(false)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppContainerPreview() {
    UnitConverterTheme {
        AppContainer()
    }
}
