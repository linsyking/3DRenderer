package name.renderer.bevy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformScreen(
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit,
    onBack: () -> Unit
) {
    val insets = WindowInsets.systemBars.asPaddingValues()

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
                title = { Text("Transform") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                // Display the 3D view.
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

                // Display the static text box on top of the 3D view.
                if (appState.text.isNotEmpty()) {
                    StaticTextBox(
                        state = appState
                    )
                }
            }

            // A placeholder section for future transform controls.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Transform Controls Placeholder",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
