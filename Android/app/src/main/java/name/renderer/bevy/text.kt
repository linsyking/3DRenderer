package name.renderer.bevy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

// A composable function to display a draggable and resizable text box.
@Composable
fun DraggableResizableTextBox(
    state: AppState,
    onStateChange: (AppState) -> Unit
) {
    // The minimum size of the text box.
    val minSize = 20.dp
    // Use rememberUpdatedState to get the latest state value within pointerInput.
    val currentAppState by rememberUpdatedState(state)

    Box(
        modifier = Modifier
            // Use the state values directly, no local state needed for offset or size.
            .offset { IntOffset(state.offsetX.roundToInt(), state.offsetY.roundToInt()) }
            .size(state.boxWidth.dp, state.boxHeight.dp)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                RoundedCornerShape(4.dp)
            )
            // Reduced padding from 8.dp to 4.dp to give more room for the text.
            .padding(4.dp)
            // This pointerInput block handles dragging the text box.
            // The `rememberUpdatedState` ensures that `currentAppState` is always the most recent state,
            // preventing the drag from "snapping" back to a previous position.
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Calculate the new position based on the latest state.
                    val newOffsetX = currentAppState.offsetX + dragAmount.x
                    val newOffsetY = currentAppState.offsetY + dragAmount.y
                    // Update the parent state with the new position, preserving all other properties.
                    onStateChange(currentAppState.copy(offsetX = newOffsetX, offsetY = newOffsetY))
                }
            }
    ) {
        // The text input field itself.
        TextField(
            value = state.text,
            onValueChange = { onStateChange(state.copy(text = it)) },
            modifier = Modifier.fillMaxSize(),
            placeholder = { Text("Text") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(
                color = state.color.copy(alpha = state.opacity),
                fontWeight = if (state.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (state.isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (state.isUnderlined) TextDecoration.Underline else TextDecoration.None,
                textAlign = state.alignment,
                lineHeight = state.lineHeight.sp
            )
        )

        // The resize handle in the bottom-right corner.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 8.dp))
                // This pointerInput block handles resizing the text box.
                // It also uses `rememberUpdatedState` to get the latest state.
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Calculate the new size based on the latest state.
                        // The minimum size is enforced here.
                        val newWidth = (currentAppState.boxWidth + dragAmount.x).coerceAtLeast(minSize.toPx())
                        val newHeight = (currentAppState.boxHeight + dragAmount.y).coerceAtLeast(minSize.toPx())
                        // Update the parent state with the new size, preserving all other properties.
                        onStateChange(currentAppState.copy(boxWidth = newWidth, boxHeight = newHeight))
                    }
                }
        )
    }
}

/**
 * The main composable function for the TextScreen.
 * This version now includes a clickable font selector.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextScreen(
    appState: AppState, // Receives the app state.
    onUpdateAppState: (AppState) -> Unit, // Receives a callback to update the app state.
    onBack: () -> Unit, // Callback to go back to the main page.
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var fontDropdownExpanded by remember { mutableStateOf(false) } // State for the font dropdown menu
    val availableFonts = listOf("Times", "Arial", "Courier New") // A list of placeholder fonts

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
            // Header with a back button.
            TopAppBar(
                title = { Text("Text Edit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )

            // Main canvas for the 3D view and the text box.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                // This is a placeholder for the actual 3D view.
                AndroidView(
                    factory = { ctx ->
                        // Access the global variable, create a new instance if null.
                        if (surfaceView == null) {
                            BevySurfaceView(context = ctx).also { surfaceView = it }
                        } else {
                            surfaceView!!
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )

                // Display a draggable and resizable text box.
                DraggableResizableTextBox(
                    state = appState,
                    onStateChange = onUpdateAppState
                )
            }

            // Text editing properties section.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Color property.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Color", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(appState.color, shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                            .clickable { showColorPickerDialog = true }
                    )
                }

                // Opacity slider.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Opacity", modifier = Modifier.weight(1f))
                    Slider(
                        value = appState.opacity,
                        onValueChange = { onUpdateAppState(appState.copy(opacity = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }

                // Font and style.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Font", modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.weight(3f)) {
                        Row(
                            modifier = Modifier.clickable { fontDropdownExpanded = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = appState.fontName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select Font"
                            )
                        }
                        // Dropdown menu for font selection
                        DropdownMenu(
                            expanded = fontDropdownExpanded,
                            onDismissRequest = { fontDropdownExpanded = false }
                        ) {
                            availableFonts.forEach { font ->
                                DropdownMenuItem(
                                    text = { Text(font) },
                                    onClick = {
                                        onUpdateAppState(appState.copy(fontName = font))
                                        fontDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    // Style buttons (Bold, Italic, Underline)
                    Row(modifier = Modifier.weight(3f)) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { onUpdateAppState(appState.copy(isBold = !appState.isBold)) }) {
                            Icon(
                                painterResource(id = R.drawable.bold),
                                contentDescription = "Bold",
                                tint = if (appState.isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { onUpdateAppState(appState.copy(isItalic = !appState.isItalic)) }) {
                            Icon(
                                painterResource(id = R.drawable.italic),
                                contentDescription = "Italic",
                                tint = if (appState.isItalic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { onUpdateAppState(appState.copy(isUnderlined = !appState.isUnderlined)) }) {
                            Icon(
                                painterResource(id = R.drawable.underline),
                                contentDescription = "Underline",
                                tint = if (appState.isUnderlined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Alignment.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Alignment", modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.weight(3f),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(onClick = { onUpdateAppState(appState.copy(alignment = TextAlign.Left)) }) {
                            Icon(
                                painterResource(id = R.drawable.align_left),
                                contentDescription = "Align Left",
                                tint = if (appState.alignment == TextAlign.Left) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { onUpdateAppState(appState.copy(alignment = TextAlign.Center)) }) {
                            Icon(
                                painterResource(id = R.drawable.align_center),
                                contentDescription = "Align Center",
                                tint = if (appState.alignment == TextAlign.Center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { onUpdateAppState(appState.copy(alignment = TextAlign.Right)) }) {
                            Icon(
                                painterResource(id = R.drawable.align_right),
                                contentDescription = "Align Right",
                                tint = if (appState.alignment == TextAlign.Right) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Line height slider.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Line spacing", modifier = Modifier.weight(1f))
                    Slider(
                        value = appState.lineHeight,
                        onValueChange = { onUpdateAppState(appState.copy(lineHeight = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0.5f..2.0f
                    )
                }
            }

            // Display the color picker dialog if showColorPickerDialog is true
            if (showColorPickerDialog) {
                ColorPickerDialog(
                    currentColor = appState.color,
                    onColorSelected = { newColor ->
                        onUpdateAppState(appState.copy(color = newColor))
                        showColorPickerDialog = false
                    },
                    onDismiss = { showColorPickerDialog = false }
                )
            }
        }
    }
}
