package name.renderer.bevy

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt


@Serializable
data class BObject(
    val data: String,
    val type: String,
    val label: String,
    val color: List<Float>,
    val pos: List<Float>,
    val scale: Float,
)

@Serializable
data class Scene(
    val objects : List<BObject> = listOf(),
    val cameraPos : List<Float> = listOf()
)

// Final AppState data class with all properties
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
    val textColor: Color = Color.Black,
    val textOpacity: Float = 1.0f,
    val meshColor: Color = Color.Black,
    val meshOpacity: Float = 1.0f,
    val meshMetallic: Float = 0.0f,
    val meshRoughness: Float = 0.5f,
    val backgroundColor: Color = Color.White,
    val environmentLightColor: Color = Color.White,
    val moveStrength: Float = 0.01f,
    val fontName: String = "Times",
    val viewScale: Float = 1.0f,
    val viewOffsetX: Float = 0f,
    val viewOffsetY: Float = 0f,
    val polygonColor: Color = Color.Blue,
    val polygonOpacity: Float = 1.0f,
    val polygonMetallic: Float = 0.0f,
    val polygonRoughness: Float = 0.5f,
    val shapeColor: Color = Color. Blue,
    val shapeOpacity: Float = 1.0f,
    val shapeMetallic: Float = 0.0f,
    val shapeRoughness: Float = 0.5f,
    val polylineColor: Color = Color. Blue,
    val polylineOpacity: Float = 1.0f,
    val curveColor: Color=Color.Blue,
    val curveOpacity: Float=1.0f,
    val scene: Scene = Scene()
)

// Global variable for BevySurfaceView to share across Composables
var surfaceView: BevySurfaceView? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

var globalAppState : AppState ? = null

@Composable
fun MyApp() {
    val navController = rememberNavController()
    // Top-level state, passed down to child Composables via callbacks
    var appState by remember { mutableStateOf(AppState()) }
    globalAppState = appState

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
            SettingsScreen(
                appState = appState,
                onUpdateAppState = { newState -> appState = newState },
                onBack = { navController.popBackStack() }
            )
        }
        composable("toolbox") {
            ToolboxScreen(
                onBack = { navController.navigateUp() },
                onEditClick = { navController.navigate("edit") },
                onTextClick = { navController.navigate("text") },
                onTransformClick = { navController.navigate("transform") },
                onViewClick = { navController.navigate("view") },
                onPolygonClick={navController.navigate("polygon")},
                onShapeClick={navController.navigate("shape")},
                onPolylineClick={navController.navigate("polyline")},
                onCurveClick={navController.navigate("curve")},
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
        composable("view") {
            ViewScreen(
                appState = appState,
                onUpdateAppState = { newState -> appState = newState },
                onBack = { navController.navigateUp() }
            )
        }
        composable("polygon") {
            PolygonScreen(
                appState = appState,
                onUpdateAppState = { newState -> appState = newState },
                onBack = { navController.navigateUp() }
            )
        }
        composable("shape") {
            ShapeScreen(
                appState = appState,
                onUpdateAppState = { newState -> appState = newState },
                onBack = { navController.navigateUp() }
            )
        }
        composable("polyline") {
            PolylineScreen(
                appState = appState,
                onUpdateAppState = { newState -> appState = newState },
                onBack = { navController.navigateUp() }
            )
        }
        composable("curve") {
             CurveScreen(
                appState = appState,
                onUpdateAppState = { newState -> appState = newState },
                onBack = { navController.navigateUp() }
            )
        }
    }
}

fun listToColor(cs: List<Float>) : Color {
    return Color(red = cs[0], green = cs[1], blue = cs[2])
}

// The main surface card of the application
@Composable
fun SurfaceCard(
    navController: NavHostController,
    appState: AppState,
    onUpdateAppState: (AppState) -> Unit
) {
    val context = LocalContext.current
    val insets = WindowInsets.systemBars.asPaddingValues()
    var expanded by remember { mutableStateOf(false) }

    val importMeshLaunch = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                // User has selected a file.
                val inputStream = context.contentResolver.openInputStream(uri)
                surfaceView?.let {
                    surfaceView ->
                    inputStream?.let { inputStream ->
                        val bytes = inputStream.readBytes()
                        val text = String(bytes, Charsets.UTF_8)
                        // Update state
                        val oldObjs = appState.scene.objects
                        val newObjs = oldObjs + BObject(
                            data = text,
                            color = listOf(1.0f, 1.0f, 1.0f),
                            type = "mesh",
                            label = "none",
                            pos = listOf(0.0f, 0.0f, 0.0f),
                            scale = 1f
                        )
                        val oldscene = appState.scene
                        onUpdateAppState(appState.copy(scene = oldscene.copy(objects = newObjs)))
                    }
                }
            }
        }
    )

    val importFileLaunch = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                // User has selected a file.
                val inputStream = context.contentResolver.openInputStream(uri)
                surfaceView?.let {
                        surfaceView ->
                    inputStream?.let { inputStream ->
                        val bytes = inputStream.readBytes()
                        val text = String(bytes, Charsets.UTF_8)
                        // Update file
                        val myopts = Json.decodeFromString<AppInitOpts>(text)
                        onUpdateAppState(appState.copy(scene = myopts.scene,
                            backgroundColor = listToColor(myopts.backgroundColor),
                            environmentLightColor = listToColor(myopts.environmentLightColor),
                            moveStrength = myopts.moveStrength
                        ))
                    }
                }
            }
        }
    )

    // Launcher for SAVING files (Save Scene, Export Image)
    val exportSceneLaunch = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
        onResult = { uri: Uri? ->
            uri?.let {
                // User has created a file.
                val outputStream = context.contentResolver.openOutputStream(uri)
                val opts = packAppInitOpts(appState)
                val text = Json.encodeToString(opts)
                outputStream?.use {
                    it.write(text.toByteArray(Charsets.UTF_8))
                }
            }
        }
    )

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
                // [MODIFICATION 1] Menu icon with text
                Column(
                    modifier = Modifier.clickable { expanded = true },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
//                    Text(text = "Menu", fontSize = 10.sp)
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Scene") },
                            onClick = {
                                importFileLaunch.launch(arrayOf("*/*"))
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Save Scene") },
                            onClick = {
                                exportSceneLaunch.launch("MyScene.scene")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Mesh") },
                            onClick = {
                                importMeshLaunch.launch(arrayOf("*/*"))
                                expanded = false
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                navController.navigate("settings")
                                expanded = false
                            }
                        )
                    }
                }

                Text(
                    text = "3D Renderer",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.titleMedium
                )

                // [MODIFICATION 2] Settings icon with text
                Column(
                    modifier = Modifier.clickable { navController.navigate("settings") },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
//                    Text(text = "Settings", fontSize = 10.sp)
                }
            }

//            Spacer(modifier = Modifier.height(8.dp))

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

        // [MODIFICATION 3] Toolbox icon with text
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
//                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.clickable { navController.navigate("toolbox") },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.toolbox),
                    contentDescription = "Toolbox"
                )
//                Text(text = "Toolbox", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// Static text box to display text on the main screen
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
                color = state.textColor.copy(alpha = state.textOpacity),
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