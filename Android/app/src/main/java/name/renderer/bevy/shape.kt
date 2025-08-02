package name.renderer.bevy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeScreen(
    onBack: () -> Unit,
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    var showColorDialog by remember { mutableStateOf(false) }

    if (showColorDialog) {
        // MODIFICATION: Updated the call to use the new ColorPickerDialog signature
        ColorPickerDialog(
            currentColor = appState.shapeColor, // Pass the current polygon color
            onDismiss = { showColorDialog = false },
            onColorSelected = {
                onUpdateAppState(appState.copy(shapeColor = it))
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
            TopAppBar(
                title = { Text("Shape Editing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )

            // The rest of the screen content remains the same
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Color", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(appState.shapeColor)
                            .clickable { showColorDialog = true }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Opacity", modifier = Modifier.weight(1f))
                    Slider(
                        value = appState.shapeOpacity,
                        onValueChange = { onUpdateAppState(appState.copy(shapeOpacity = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Metal", modifier = Modifier.weight(1f))
                    Slider(
                        value = appState.shapeMetallic,
                        onValueChange = { onUpdateAppState(appState.copy(shapeMetallic = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Gloss", modifier = Modifier.weight(1f))
                    Slider(
                        value = appState.shapeRoughness,
                        onValueChange = { onUpdateAppState(appState.copy(shapeRoughness = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }
            }
        }
    }
}