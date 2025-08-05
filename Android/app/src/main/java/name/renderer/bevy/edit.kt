package name.renderer.bevy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    onBack: () -> Unit,
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    var showColorDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedMesh by remember { mutableStateOf("Mesh 1") }
    val meshOptions = listOf("Mesh 1", "Mesh 2", "Mesh 3") // Placeholder

    if (showColorDialog) {
        ColorPickerDialog(
            currentColor = appState.meshColor,
            onDismiss = { showColorDialog = false },
            onColorSelected = {
                onUpdateAppState(appState.copy(meshColor = it))
                showColorDialog = false
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = { Text("Mesh Editing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )

            // Mesh selection dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Selected Mesh:", modifier = Modifier.weight(1f))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = selectedMesh,
                        onValueChange = {},
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        meshOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedMesh = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        surfaceView ?: BevySurfaceView(context = ctx).also { surfaceView = it }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Color picker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Color", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(appState.meshColor)
                            .clickable { showColorDialog = true }
                    )
                }

                // Position X
                PositionControl(
                    label = "Position X",
                    value = appState.offsetX,
                    onValueChange = { onUpdateAppState(appState.copy(offsetX = it)) },
                    range = -100f..100f
                )

                // Position Y
                PositionControl(
                    label = "Position Y",
                    value = appState.offsetY,
                    onValueChange = { onUpdateAppState(appState.copy(offsetY = it)) },
                    range = -100f..100f
                )

                // Position Z
                PositionControl(
                    label = "Position Z",
                    value = appState.offsetZ,
                    onValueChange = { onUpdateAppState(appState.copy(offsetZ = it))},
                    range = -100f..100f
                )
            }
        }
    }
}

@Composable
private fun PositionControl(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    var textValue by remember { mutableStateOf(value.toString()) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = textValue,
            onValueChange = {
                textValue = it
                it.toFloatOrNull()?.let { floatValue ->
                    if (floatValue in range) {
                        onValueChange(floatValue)
                    }
                }
            },
            modifier = Modifier.width(80.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Slider(
            value = value,
            onValueChange = {
                onValueChange(it)
                textValue = it.toString()
            },
            modifier = Modifier.weight(2f),
            valueRange = range
        )
    }
}