package name.renderer.bevy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The SettingsScreen composable function.
 * This screen allows the user to adjust various application settings.
 *
 * @param appState The current state of the application.
 * @param onUpdateAppState A callback function to update the application state.
 * @param onBack A callback function to handle navigating back from this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit,
    onBack: () -> Unit
) {
    val insets = WindowInsets.systemBars.asPaddingValues()

    // State to control the visibility of the color picker dialog
    var showColorPickerDialog by remember { mutableStateOf(false) }
    // State to track which color picker is being shown
    var selectedColorTarget by remember { mutableStateOf(ColorTarget.None) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with a back button
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )

            // Main content area for settings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Canvas Width setting
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(text = "Canvas width", modifier = Modifier.weight(1f))
//                    // Text field for canvas width with number input
//                    TextField(
//                        value = appState.canvasWidth.toInt().toString(),
//                        onValueChange = {
//                            // Update the state only if the input is a valid number
//                            val newWidth = it.toFloatOrNull()
//                            if (newWidth != null) {
//                                onUpdateAppState(appState.copy(canvasWidth = newWidth))
//                            }
//                        },
//                        modifier = Modifier.weight(1f),
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                    )
//                }
//
//                // Canvas Height setting
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(text = "Canvas height", modifier = Modifier.weight(1f))
//                    // Text field for canvas height with number input
//                    TextField(
//                        value = appState.canvasHeight.toInt().toString(),
//                        onValueChange = {
//                            // Update the state only if the input is a valid number
//                            val newHeight = it.toFloatOrNull()
//                            if (newHeight != null) {
//                                onUpdateAppState(appState.copy(canvasHeight = newHeight))
//                            }
//                        },
//                        modifier = Modifier.weight(1f),
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                    )
//                }

                // Background Color setting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Background color", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(appState.backgroundColor, shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                            .clickable {
                                selectedColorTarget = ColorTarget.BackgroundColor
                                showColorPickerDialog = true
                            }
                    )
                }

                // Environment Light Color setting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Environment light color", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(appState.environmentLightColor, shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                            .clickable {
                                selectedColorTarget = ColorTarget.EnvironmentLight
                                showColorPickerDialog = true
                            }
                    )
                }

                // Environment Light Strength slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Moving strength", modifier = Modifier.weight(1f))
                    Slider(
                        value = appState.moveStrength,
                        onValueChange = { onUpdateAppState(appState.copy(moveStrength = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..0.02f
                    )
                }
            }

            // Display the color picker dialog if showColorPickerDialog is true
            if (showColorPickerDialog) {
                val currentColor = when (selectedColorTarget) {
                    ColorTarget.BackgroundColor -> appState.backgroundColor
                    ColorTarget.EnvironmentLight -> appState.environmentLightColor
                    else -> Color.White // Default color
                }
                ColorPickerDialog(
                    currentColor = currentColor,
                    onColorSelected = { newColor ->
                        when (selectedColorTarget) {
                            ColorTarget.BackgroundColor -> {
                                onUpdateAppState(appState.copy(backgroundColor = newColor))
                            }
                            ColorTarget.EnvironmentLight -> {
                                onUpdateAppState(appState.copy(environmentLightColor = newColor))
                            }
                            else -> {}
                        }
                        showColorPickerDialog = false
                    },
                    onDismiss = { showColorPickerDialog = false }
                )
            }
        }
    }
}

// Enum to specify which color is being edited
enum class ColorTarget {
    None,
    BackgroundColor,
    EnvironmentLight
}
