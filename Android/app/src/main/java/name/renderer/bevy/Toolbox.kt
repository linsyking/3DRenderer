package name.renderer.bevy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ToolboxScreen(onBack: () -> Unit) {
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
            // Header for the toolbox with a back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
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
                        // All icons are now the toolbox drawable resource
                        ToolboxItem("undo", R.drawable.undo),
                        ToolboxItem("redo", R.drawable.redo),
                        ToolboxItem("view", R.drawable.view),
                        ToolboxItem("edit", R.drawable.edit),
                    )
                )

                // Row 2: Text, Light, Transform
                ToolboxRow(
                    items = listOf(
                        // All icons are now the toolbox drawable resource
                        ToolboxItem("text", R.drawable.text),
                        ToolboxItem("light", R.drawable.light),
                        ToolboxItem("transform", R.drawable.transform),
                    )
                )

                // Row 3: Polygon, Polyline, Shape, Curve
                ToolboxRow(
                    items = listOf(
                        // All icons are now the toolbox drawable resource
                        ToolboxItem("polygon", R.drawable.polygon),
                        ToolboxItem("polyline", R.drawable.polyline),
                        ToolboxItem("shape", R.drawable.shape),
                        ToolboxItem("curve", R.drawable.curve),
                    )
                )
            }
        }
    }
}

// A data class to hold icon and text information for each toolbox item
// The icon is now an Int representing the drawable resource ID
data class ToolboxItem(
    val text: String,
    val icon: Int
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
                onClick = { /* TODO: Handle button click for ${item.text} */ }
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
