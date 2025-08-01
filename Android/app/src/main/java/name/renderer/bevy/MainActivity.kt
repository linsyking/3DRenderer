package name.renderer.bevy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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

    NavHost(navController = navController, startDestination = "main",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable("main") { SurfaceCard(navController) }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable("toolbox") {
            ToolboxScreen(onBack = { navController.navigateUp() },
                onEditClick={navController.navigate("edit")})
        }
        composable("edit"){
            EditScreen ( onBack={navController.navigateUp()})
        }
    }
}

var surfaceView: BevySurfaceView? = null

@Composable
fun SurfaceCard(navController: NavHostController) {
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
                    DropdownMenu (
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

            AndroidView(
                factory = { ctx ->
                    if (surfaceView == null) {
                        val sv = BevySurfaceView(context = ctx)
                        surfaceView = sv
                        sv
                    } else {
                        surfaceView!!
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // Toolbox Icon at the bottom of the main page
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            IconButton(onClick = {
                navController.navigate("toolbox")
            }) {
                // The painterResource function handles both PNGs and Vector Drawables (.xml)
                // correctly, so the code remains the same. The key is to ensure your
                // XML file is correctly named and located in res/drawable.
                Icon(
                    painter = painterResource(id=R.drawable.toolbox),
                    contentDescription = "Toolbox"
                )
            }
        }
    }
}
