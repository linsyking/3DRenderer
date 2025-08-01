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

// 定义一个数据类，用来存储所有的应用状态
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
    val opacity: Float = 1.0f
)

// 将 BevySurfaceView 声明为全局变量，以便在 Composable 之间共享
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
    // 在顶层 Composable 中声明 AppState，以便可以传递给子页面
    var appState by remember { mutableStateOf(AppState()) }

    NavHost(
        navController = navController, startDestination = "main",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // 修改 SurfaceCard，使其接收 appState 和 onUpdateAppState
        composable("main") {
            SurfaceCard(
                navController = navController,
                appState = appState,
                onUpdateAppState = { newState -> appState = newState }
            )
        }
        // SettingsScreen 不涉及应用状态，因此不传递
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        // 修改 ToolboxScreen，使其接收 appState 和 onUpdateAppState
        composable("toolbox") {
            ToolboxScreen(
                onBack = { navController.navigateUp() },
                onEditClick = { navController.navigate("edit") },
                onTextClick = { navController.navigate("text") },
                appState = appState,
                onUpdateAppState = { newState -> appState = newState }
            )
        }
        // 修改 EditScreen，使其接收 appState 和 onUpdateAppState
        composable("edit") {
            EditScreen(
                onBack = { navController.navigateUp() },
                appState = appState,
                onUpdateAppState = { newState -> appState = newState }
            )
        }
        // TextScreen 现在接收 AppState 和一个回调函数，以实现状态提升
        composable("text") {
            TextScreen(
                appState = appState,
                onUpdateAppState = { newState -> appState = newState },
                onBack = { navController.navigateUp() }
            )
        }
    }
}

// SurfaceCard 现在需要接收 appState 和 onUpdateAppState
@Composable
fun SurfaceCard(
    navController: NavHostController,
    appState: AppState, // New parameter
    onUpdateAppState: (AppState) -> Unit // New parameter
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

            // Put AndroidView and StaticTextBox in a Box.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        // Check the global variable, if null, create and assign it.
                        if (surfaceView == null) {
                            val sv = BevySurfaceView(context = ctx)
                            surfaceView = sv
                            sv
                        } else {
                            surfaceView!!
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Render the text box on the main page, but only if the text is not empty.
                if (appState.text.isNotEmpty()) {
                    StaticTextBox(
                        state = appState
                    )
                }
            }
        }

        // Toolbox Icon at the bottom of the main page.
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

// Assumed EditScreen, now also receives appState.
@Composable
fun EditScreen(
    onBack: () -> Unit,
    appState: AppState, // New parameter
    onUpdateAppState: (AppState) -> Unit // New parameter
) {
    // Assumed UI
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Edit Screen", modifier = Modifier.align(Alignment.Center))
        IconButton(onClick = onBack) {
            // ...
        }
    }
}

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
