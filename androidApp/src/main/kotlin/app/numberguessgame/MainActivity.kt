package app.numberguessgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Jetpack AppState DataStore persistence
        // Automatically loads saved stats (games won, best score) from disk and saves every update!
        val stateStoragePath = File(filesDir, "number_guess_appstate.json").absolutePath
        AppStatePersistence.startSync(
            storagePath = stateStoragePath,
            scope = lifecycleScope
        )

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}