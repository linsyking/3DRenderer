package name.renderer.bevy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * The ToolboxScreen composable function.
 * This screen displays a grid of icons for various tools.
 *
 * @param onBack A callback function to handle navigating back from this screen.
 * @param onEditClick A callback function to handle navigating to the EditScreen.
 * @param @param onTextClick A callback function to handle navigating to the TextScreen.
 * @param appState The current state of the application.
 * @param onUpdateAppState A callback function to update the application state.
 */
@Composable
fun ToolboxScreen(
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onTextClick: () -> Unit,
//    onTransformClick: () -> Unit,
    onViewClick: () -> Unit,
    onCubeClick: () -> Unit,
    onSphereClick: () -> Unit,
//    onPolylineClick: () -> Unit,
//    onCurveClick: () -> Unit,
    appState: AppState, // Now receives the AppState.
    onUpdateAppState: (AppState) -> Unit // Now receives the state update callback.
) {
    val insets = WindowInsets.systemBars.asPaddingValues()

    // Using a Surface for the toolbox to give it a distinct background and elevation
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header for the toolbox with a back button, now using a custom drawable resource.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    // Using a custom drawable for the back button
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Toolbox",
                    style = MaterialTheme.typography.headlineSmall
                )
                // A spacer to balance the layout since we only have a back button on the left
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // The main grid-like layout for the toolbox icons
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1: Undo, Redo, View, Edit
                ToolboxRow(
                    items = listOf(
                        // All icons are now the specific drawable resources
                        ToolboxItem("undo", R.drawable.undo, onClick = { /* TODO: Handle undo */ }),
                        ToolboxItem("redo", R.drawable.redo, onClick = { /* TODO: Handle redo */ }),
                        ToolboxItem("view", R.drawable.view, onClick = onViewClick),
                        ToolboxItem("edit", R.drawable.edit, onClick = onEditClick),
                    )
                )

                // Row 2: Text, Light, Transform
                ToolboxRow(
                    items = listOf(
                        // All icons are now the specific drawable resources
                        ToolboxItem("text", R.drawable.text, onClick = onTextClick),
                        ToolboxItem("light", R.drawable.light, onClick = { /* TODO: Handle light */ }),
                        ToolboxItem("Cube", R.drawable.cube, onClick=onCubeClick),
                        ToolboxItem("Sphere", R.drawable.sphere,onClick=onSphereClick),
//                        ToolboxItem("transform", R.drawable.transform, onClick = onTransformClick),
                    )
                )

                // Row 3: Polygon, Polyline, Shape, Curve
//                ToolboxRow(
//                    items = listOf(
//                         All icons are now the specific drawable resources
//                        ToolboxItem("polygon", R.drawable.polygon, onClick = onPolygonClick),
//                        ToolboxItem("polyline", R.drawable.polyline, onClick = onPolylineClick),
//                        ToolboxItem("shape", R.drawable.shape, onClick = onShapeClick),
//                        ToolboxItem("curve", R.drawable.curve, onClick = onCurveClick),
//                    )
//                )
            }
        }
    }
}

// A data class to hold icon and text information for each toolbox item.
// The icon is now an Int representing the drawable resource ID, and a click handler is included.
data class ToolboxItem(
    val text: String,
    val icon: Int,
    val onClick: () -> Unit
)

// A reusable composable for a single row of toolbox items
@Composable
fun ToolboxRow(items: List<ToolboxItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEach { item ->
            ToolboxButton(
                item = item,
                onClick = item.onClick
            )
        }
    }
}

// A reusable composable for a single button with an icon and text
@Composable
fun ToolboxButton(item: ToolboxItem, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(IntrinsicSize.Min)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                // Use painterResource to load the drawable from the resource ID
                painter = painterResource(id = item.icon),
                contentDescription = item.text,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = item.text,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
