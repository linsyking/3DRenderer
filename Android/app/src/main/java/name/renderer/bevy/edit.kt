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

/**
 * 这是颜色选择器对话框的占位符。
 * 在实际应用中，您会使用一个合适的颜色选择器库。
 */
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
                // 示例颜色
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

/**
 * 显示编辑界面的 Composable 函数。
 * 它显示 3D 对象并提供滑块来编辑其属性。
 *
 * @param onBack 回调函数，用于从该屏幕返回。
 * @param appState 当前的应用状态。
 * @param onUpdateAppState 回调函数，用于更新应用状态。
 */
@Composable
fun EditScreen(
    onBack: () -> Unit,
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit
) {
    val insets = WindowInsets.systemBars.asPaddingValues()

    var showColorDialog by remember { mutableStateOf(false) }

    if (showColorDialog) {
        ColorPickerDialog(
            onDismissRequest = { showColorDialog = false },
            onColorSelected = {
                // 使用 onUpdateAppState 回调更新颜色状态
                onUpdateAppState(appState.copy(color = it))
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
            // 带有返回按钮的标题
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

            // 3D 对象视图的占位符
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                // 使用全局 surfaceView
                AndroidView(
                    factory = { ctx ->
                        surfaceView ?: BevySurfaceView(context = ctx).also { surfaceView = it }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )

                // 在编辑界面也渲染文本
                if (appState.text.isNotEmpty()) {
                    StaticTextBox(state = appState)
                }
            }

            // 编辑属性部分
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 颜色属性
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Color", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            // 从 appState 获取当前颜色
                            .background(appState.color)
                            .clickable { showColorDialog = true }
                    )
                }

                // 不透明度滑块
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Opacity", modifier = Modifier.weight(1f))
                    Slider(
                        // 从 appState 获取当前不透明度值
                        value = appState.opacity,
                        onValueChange = { onUpdateAppState(appState.copy(opacity = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }

                // 金属度滑块
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Metal", modifier = Modifier.weight(1f))
                    Slider(
                        value = appState.metallic,
                        onValueChange = { onUpdateAppState(appState.copy(metallic = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }

                // 粗糙度（光泽）滑块
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Gloss", modifier = Modifier.weight(1f))
                    Slider(
                        value = appState.roughness,
                        onValueChange = { onUpdateAppState(appState.copy(roughness = it)) },
                        modifier = Modifier.weight(3f),
                        valueRange = 0f..1f
                    )
                }
            }
        }
    }
}
