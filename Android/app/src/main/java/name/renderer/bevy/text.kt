package name.renderer.bevy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * A composable function to display the TextScreen.
 * It shows the 3D object and provides controls to edit text properties.
 *
 * @param onBack A callback function to handle navigating back from this screen.
 */
@Composable
fun TextScreen(onBack: () -> Unit) {
    val insets = WindowInsets.systemBars.asPaddingValues()

    // State variables for text properties
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var opacity by remember { mutableStateOf(1.0f) }
    var fontName by remember { mutableStateOf("Times") }
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var isUnderlined by remember { mutableStateOf(false) }
    var alignment by remember { mutableStateOf(TextAlign.Center) }
    var lineSpacing by remember { mutableStateOf(1.0f) }

    var showColorDialog by remember { mutableStateOf(false) }

    // State for editable text content
    var editableText by remember { mutableStateOf("Text") }

    // States for drag offset (for moving the box)
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // States for text box size (for resizing)
    var boxWidth by remember { mutableStateOf(200.dp) }
    var boxHeight by remember { mutableStateOf(100.dp) }
    val minSize = 50.dp // Minimum size for the text box

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
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Text Editing",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            // Placeholder for the 3D object view with editable text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // This is a placeholder for the actual 3D view
                AndroidView(
                    factory = { ctx ->
                        surfaceView ?: BevySurfaceView(context = ctx).also { surfaceView = it }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )

                // The resizable and draggable text box
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .size(boxWidth, boxHeight)
                        .border(
                            BorderStroke(1.dp, Color.Black),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                        .pointerInput(Unit) {
                            // Drag gesture for moving the box
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        }
                ) {
                    // The text field itself
                    TextField(
                        value = editableText,
                        onValueChange = { editableText = it },
                        modifier = Modifier
                            .fillMaxSize(),
                        placeholder = { Text("Text") },
                        textStyle = TextStyle(
                            color = selectedColor.copy(alpha = opacity),
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = if (isUnderlined) TextDecoration.Underline else TextDecoration.None,
                            textAlign = alignment,
                            lineHeight = lineSpacing.sp
                        )
                    )

                    // Resizing handle in the bottom-right corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 8.dp))
                            .pointerInput(Unit) {
                                // Drag gesture for resizing the box
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    boxWidth = (boxWidth.toPx() + dragAmount.x).coerceAtLeast(minSize.toPx()).toDp()
                                    boxHeight = (boxHeight.toPx() + dragAmount.y).coerceAtLeast(minSize.toPx()).toDp()
                                }
                            }
                    )
                }
            }

            // Text editing attributes section
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
                    Text(text = "color", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(selectedColor, shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                            .clickable { showColorDialog = true }
                    )
                }

                // Opacity slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "opacity", modifier = Modifier.weight(1f))
                    Slider(
                        value = opacity,
                        onValueChange = { opacity = it },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }

                // Font and style
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "font", modifier = Modifier.weight(1f))
                    Row(modifier = Modifier.weight(3f)) {
                        Text(text = fontName, modifier = Modifier.align(Alignment.CenterVertically))
                        Spacer(Modifier.width(8.dp))
                        // Bold button
                        IconButton(onClick = { isBold = !isBold }) {
                            Icon(
                                painterResource(id = R.drawable.bold),
                                contentDescription = "Bold",
                                tint = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Italic button
                        IconButton(onClick = { isItalic = !isItalic }) {
                            Icon(
                                painterResource(id = R.drawable.italic),
                                contentDescription = "Italic",
                                tint = if (isItalic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Underline button
                        IconButton(onClick = { isUnderlined = !isUnderlined }) {
                            Icon(
                                painterResource(id = R.drawable.underline),
                                contentDescription = "Underline",
                                tint = if (isUnderlined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Alignment
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "alignment", modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.weight(3f),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(onClick = { alignment = TextAlign.Left }) {
                            Icon(
                                painterResource(id = R.drawable.align_left),
                                contentDescription = "Align Left",
                                tint = if (alignment == TextAlign.Left) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { alignment = TextAlign.Center }) {
                            Icon(
                                painterResource(id = R.drawable.align_center),
                                contentDescription = "Align Center",
                                tint = if (alignment == TextAlign.Center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { alignment = TextAlign.Right }) {
                            Icon(
                                painterResource(id = R.drawable.align_right),
                                contentDescription = "Align Right",
                                tint = if (alignment == TextAlign.Right) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Line spacing slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "line spacing", modifier = Modifier.weight(1f))
                    Slider(
                        value = lineSpacing,
                        onValueChange = { lineSpacing = it },
                        modifier = Modifier.weight(3f),
                        valueRange = 0.5f..2.0f
                    )
                }
            }
        }
    }
}
