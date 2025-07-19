package name.jinleili.bevy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import name.jinleili.bevy.ui.theme.MyApplicationTheme
import name.jinleili.bevy.R

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.white)
                ) {
                    SurfaceCard()
                }
            }
        }
    }
}

var surfaceView: BevySurfaceView? = null

@Composable
fun SurfaceCard() {
    val insets = WindowInsets.systemBars.asPaddingValues()
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Column(modifier = Modifier.fillMaxSize().padding(insets)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .height(44.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Bevy in Android App", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        AndroidView(
            factory = { ctx ->
                val sv = BevySurfaceView(context = ctx)
                surfaceView = sv
                sv
            },
            modifier = Modifier
                .fillMaxWidth()
                .height((screenWidth.toFloat() * 1.6).dp),
        )
    }
}
