package name.renderer.bevy

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView

/**
 * The ViewScreen composable function.
 * This screen displays the 3D render and allows the user to pan and zoom the view.
 *
 * @param appState The current state of the application.
 * @param onUpdateAppState A callback function to update the application state.
 * @param onBack A callback function to handle navigating back from this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScreen(
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit,
    onBack: () -> Unit
) {
    // 记住最新的 appState，以防止在手势检测中用到过时的状态
    val currentAppState by rememberUpdatedState(appState)
    val insets = WindowInsets.systemBars.asPaddingValues()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部栏，包含返回按钮和重置按钮
            TopAppBar(
                title = { Text("View Control") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // 重置按钮，将缩放和位移恢复到默认值
                    IconButton(onClick = {
                        onUpdateAppState(
                            appState.copy(
                                viewScale = 1.0f,
                                viewOffsetX = 0f,
                                viewOffsetY = 0f
                            )
                        )
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.restart), // 假设你有一个重置图标
                            contentDescription = "Reset View"
                        )
                    }
                }
            )

            // 主要内容区域，用于显示渲染视图并处理手势
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds() // 确保渲染视图不会画到Box范围之外
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 1. 使用 graphicsLayer 来应用缩放和位移变换，性能很高
                        .graphicsLayer {
                            scaleX = appState.viewScale
                            scaleY = appState.viewScale
                            translationX = appState.viewOffsetX
                            translationY = appState.viewOffsetY
                        }
                        // 2. 使用 pointerInput 和 detectTransformGestures 来同时处理拖动和缩放
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                // 计算新的缩放值，并限制在合理范围内 (例如0.5倍到5倍)
                                val newScale = (currentAppState.viewScale * zoom)
                                    .coerceIn(0.5f, 5f)

                                // 计算新的位移值
                                val newOffsetX = currentAppState.viewOffsetX + pan.x
                                val newOffsetY = currentAppState.viewOffsetY + pan.y

                                // 3. 通过 onUpdateAppState 回调来更新全局状态
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
                    // 嵌入你的 BevySurfaceView
                    AndroidView(
                        factory = { ctx ->
                            surfaceView ?: BevySurfaceView(context = ctx)
                                .also { surfaceView = it }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}