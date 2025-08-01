package name.renderer.bevy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

// This is a placeholder for the color picker dialog.
// In a real application, you would use a proper color picker library.
@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Select Color") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Example colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color)
                                .clickable { onColorSelected(color) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text("Dismiss")
            }
        }
    )
}

// This is a placeholder for the BevySurfaceView to display the 3D object.
// You should replace this with your actual implementation that shows the editable object.
//var surfaceView: BevySurfaceView? = null

@Composable
fun EditScreen(onBack: () -> Unit) {
    val insets = WindowInsets.systemBars.asPaddingValues()

    // State variables to save the attributes
    var selectedColor by remember { mutableStateOf(Color.White) }
    var opacity by remember { mutableStateOf(1.0f) }
    var metallic by remember { mutableStateOf(0.0f) }
    var roughness by remember { mutableStateOf(0.5f) } // Using roughness as a more common PBR term than 'gloss'

    var showColorDialog by remember { mutableStateOf(false) }

    if (showColorDialog) {
        ColorPickerDialog(
            onDismissRequest = { showColorDialog = false },
            onColorSelected = {
                selectedColor = it
                showColorDialog = false
            }
        )
    }

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
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Mesh Editing",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            // Placeholder for the 3D object view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                // Reinstating AndroidView with BevySurfaceView.
                AndroidView(
                    factory = { ctx ->
                        if (surfaceView == null) {
                            val sv = BevySurfaceView(context = ctx)
                            surfaceView = sv
                            sv
                        } else {
                            surfaceView!!
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }

            // Editing attributes section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Color attribute
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Color", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(selectedColor)
                            .clickable { showColorDialog = true }
                    )
                }

                // Opacity slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Opacity", modifier = Modifier.weight(1f))
                    Slider(
                        value = opacity,
                        onValueChange = { opacity = it },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }

                // Metallic slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Metal", modifier = Modifier.weight(1f))
                    Slider(
                        value = metallic,
                        onValueChange = { metallic = it },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }

                // Roughness (Gloss) slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Gloss", modifier = Modifier.weight(1f))
                    Slider(
                        value = roughness,
                        onValueChange = { roughness = it },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }
            }
        }
    }
}
