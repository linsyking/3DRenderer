package name.renderer.bevy

import android.util.Log
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
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurveScreen(
    onBack: () -> Unit,
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    var showColorDialog by remember { mutableStateOf(false) }


    if (showColorDialog) {
        // MODIFICATION: Updated the call to use the new ColorPickerDialog signature
        ColorPickerDialog(
            currentColor = appState.curveColor, // Pass the current curve color
            onDismiss = { showColorDialog = false },
            onColorSelected = {
                onUpdateAppState(appState.copy(curveColor = it))
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
                title = { Text("Curve Editing") },
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
                            .background(appState.curveColor)
                            .clickable { showColorDialog = true }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Tube Size", modifier = Modifier.weight(1f))
                    Slider(
                        value = appState.curveOpacity,
                        onValueChange = { onUpdateAppState(appState.copy(curveOpacity = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..5f
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appState.sketchMode) "Move Mode" else "Sketch Mode",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = appState.sketchMode,
                        onCheckedChange = { nmode ->
                            surfaceView?.let { surfaceView ->
                                RustBridge.switch_mode(surfaceView.bevy_app, if (nmode) 1 else 0)
                            }

                            onUpdateAppState(appState.copy(sketchMode = nmode)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        surfaceView?.let { surfaceView ->
                            val res = RustBridge.get_mesh(surfaceView.bevy_app)
                            val obj = Json.decodeFromString<BObject>(res)
                            val oldobjects = appState.scene.objects
                            val nobjs = oldobjects + obj
                            onUpdateAppState(appState.copy(scene = appState.scene.copy(objects = nobjs)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }



            }
        }
    }
}