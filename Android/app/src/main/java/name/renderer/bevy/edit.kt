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
    var showColorDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedMesh by remember { mutableIntStateOf(0) }
    val meshOptions = List(appState.scene.objects.size) { i -> "Mesh ${i + 1}" }

    if (showColorDialog) {
        ColorPickerDialog(
            currentColor = appState.meshColor,
            onDismiss = { showColorDialog = false },
            onColorSelected = { mcolor ->
                onUpdateAppState(appState.copy(meshColor = mcolor))
                showColorDialog = false
                val objectdd = appState.scene.objects[selectedMesh]
                val nobj = objectdd.copy(color = colorToList(mcolor))
                val nobjs = appState.scene.objects.mapIndexed {  i, v -> if (i == selectedMesh) nobj else v  }
                onUpdateAppState(appState.copy(scene = appState.scene.copy(objects = nobjs)))
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
                        value = meshOptions[selectedMesh],
                        onValueChange = {},
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        meshOptions.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedMesh = index
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .background(Color.Gray),
//                contentAlignment = Alignment.Center
//            ) {
//                AndroidView(
//                    factory = { ctx ->
//                        surfaceView ?: BevySurfaceView(context = ctx).also { surfaceView = it }
//                    },
//                    modifier = Modifier.fillMaxSize()
//                )
//            }

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
                    value = appState.scene.objects[selectedMesh].pos[0],
                    onValueChange = { v ->
                        val objectdd = appState.scene.objects[selectedMesh]
                        val pos = objectdd.pos
                        val nobj = objectdd.copy(pos = listOf(v, pos[1], pos[2]))
                        val nobjs = appState.scene.objects.mapIndexed {  i, v -> if (i == selectedMesh) nobj else v  }
                        onUpdateAppState(appState.copy(scene = appState.scene.copy(objects = nobjs)))
                                    },
                    range = -5f..5f
                )

                // Position Y
                PositionControl(
                    label = "Position Y",
                    value = appState.scene.objects[selectedMesh].pos[1],
                    onValueChange = { v ->
                        val objectdd = appState.scene.objects[selectedMesh]
                        val pos = objectdd.pos
                        val nobj = objectdd.copy(pos = listOf(pos[0], v, pos[2]))
                        val nobjs = appState.scene.objects.mapIndexed {  i, v -> if (i == selectedMesh) nobj else v  }
                        onUpdateAppState(appState.copy(scene = appState.scene.copy(objects = nobjs)))
                    },
                    range = -5f..5f
                )

                // Position Z
                PositionControl(
                    label = "Position Z",
                    value = appState.scene.objects[selectedMesh].pos[2],
                    onValueChange = { v ->
                        val objectdd = appState.scene.objects[selectedMesh]
                        val pos = objectdd.pos
                        val nobj = objectdd.copy(pos = listOf(pos[0], pos[1], v))
                        val nobjs = appState.scene.objects.mapIndexed {  i, v -> if (i == selectedMesh) nobj else v  }
                        onUpdateAppState(appState.copy(scene = appState.scene.copy(objects = nobjs)))
                    },
                    range = -5f..5f
                )


                PositionControl(
                    label = "Scale",
                    value = appState.scene.objects[selectedMesh].scale,
                    onValueChange = { v ->
                        val objectdd = appState.scene.objects[selectedMesh]
                        val pos = objectdd.pos
                        val nobj = objectdd.copy(scale = v)
                        val nobjs = appState.scene.objects.mapIndexed {  i, v -> if (i == selectedMesh) nobj else v  }
                        onUpdateAppState(appState.copy(scene = appState.scene.copy(objects = nobjs)))
                    },
                    range = 0.1f..10f
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