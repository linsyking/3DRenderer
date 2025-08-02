package name.renderer.bevy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.math.roundToInt

// 应用程序的状态数据类
data class AppState(
    val text: String = "",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val boxWidth: Float = 200f,
    val boxHeight: Float = 100f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderlined: Boolean = false,
    val alignment: TextAlign = TextAlign.Center,
    val lineHeight: Float = 1.0f,
    val color: Color = Color.Black,
    val opacity: Float = 1.0f,
    val metallic: Float = 0.0f,
    val roughness: Float = 0.5f
)

// BevySurfaceView 的全局变量，用于在不同 Composable 之间共享
var surfaceView: BevySurfaceView? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    // 顶层状态，通过回调函数传递给子 Composable
    var appState by remember { mutableStateOf(AppState()) }

    NavHost(
        navController = navController, startDestination = "main",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable("main") {
            SurfaceCard(
                navController = navController,
                appState = appState,
                onUpdateAppState = { newState -> appState = newState }
            )
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable("toolbox") {
            ToolboxScreen(
                onBack = { navController.navigateUp() },
                onEditClick = { navController.navigate("edit") },
                onTextClick = { navController.navigate("text") },
                onTransformClick = { navController.navigate("transform") },
                appState = appState,
                onUpdateAppState = { newState -> appState = newState }
            )
        }
        composable("edit") {
            EditScreen(
                onBack = { navController.navigateUp() },
                appState = appState,
                onUpdateAppState = { newState -> appState = newState }
            )
        }
        composable("text") {
            TextScreen(
                appState = appState,
                onUpdateAppState = { newState -> appState = newState },
                onBack = { navController.navigateUp() }
            )
        }
        composable("transform") {
            TransformScreen(
                appState = appState,
                onUpdateAppState = { newState -> appState = newState },
                onBack = { navController.navigateUp() }
            )
        }
    }
}

// 主界面的 SurfaceCard
@Composable
fun SurfaceCard(
    navController: NavHostController,
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit
) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Import File") },
                            onClick = {
                                expanded = false
                                // Handle Import File
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export File") },
                            onClick = {
                                expanded = false
                                // Handle Export File
                            }
                        )
                    }
                }

                Text(
                    text = "3D Renderer",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = {
                    navController.navigate("settings")
                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        surfaceView ?: BevySurfaceView(context = ctx).also { surfaceView = it }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (appState.text.isNotEmpty()) {
                    StaticTextBox(
                        state = appState
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            IconButton(onClick = {
                navController.navigate("toolbox")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.toolbox),
                    contentDescription = "Toolbox"
                )
            }
        }
    }
}

// 静态文本框，用于在主界面显示文本
@Composable
fun StaticTextBox(
    state: AppState
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(state.offsetX.roundToInt(), state.offsetY.roundToInt()) }
            .size(state.boxWidth.dp, state.boxHeight.dp)
            .background(Color.Transparent)
            .padding(8.dp)
    ) {
        Text(
            text = state.text,
            style = TextStyle(
                color = state.color.copy(alpha = state.opacity),
                fontWeight = if (state.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (state.isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (state.isUnderlined) TextDecoration.Underline else TextDecoration.None,
                textAlign = state.alignment,
                lineHeight = state.lineHeight.sp
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}
