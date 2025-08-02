package name.renderer.bevy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScreen(
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit,
    onBack: () -> Unit
) {
    val currentAppState by rememberUpdatedState(appState)
    val insets = WindowInsets.systemBars.asPaddingValues()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("View") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Column(
                        modifier = Modifier
                            .clickable {
                                onUpdateAppState(
                                    appState.copy(
                                        viewScale = 1.0f,
                                        viewOffsetX = 0f,
                                        viewOffsetY = 0f
                                    )
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.restart),
                            contentDescription = "Restart View",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Restart",
                            fontSize = 10.sp
                        )
                    }
                }
            )

            // [新增] 在顶部添加关于重启按钮的提示文字
            Text(
                text = "Press button to restart the view.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // 主要内容区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 让这个Box填满剩余空间
                    .clipToBounds()
            ) {
                // 这个Box应用变换效果，提供UI上的视觉反馈
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = appState.viewScale
                            scaleY = appState.viewScale
                            translationX = appState.viewOffsetX
                            translationY = appState.viewOffsetY
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (currentAppState.viewScale * zoom)
                                    .coerceIn(0.5f, 5f)
                                val newOffsetX = currentAppState.viewOffsetX + pan.x
                                val newOffsetY = currentAppState.viewOffsetY + pan.y

                                onUpdateAppState(
                                    currentAppState.copy(
                                        viewScale = newScale,
                                        viewOffsetX = newOffsetX,
                                        viewOffsetY = newOffsetY
                                    )
                                )
                            }
                        }
                ) {
                    AndroidView(
                        factory = { ctx ->
                            surfaceView ?: BevySurfaceView(context = ctx)
                                .also { surfaceView = it }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // [恢复] 恢复底部的操作说明文字
            Text(
                text = "Use two fingers to zoom, and one finger to pan the view.",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}