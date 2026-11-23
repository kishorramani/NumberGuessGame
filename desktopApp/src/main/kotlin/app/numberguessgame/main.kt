package app.numberguessgame

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

fun main() {
    // Initialize Jetpack AppState DataStore persistence for Desktop
    val storageDir = File(System.getProperty("user.home"), ".numberguessgame").apply { mkdirs() }
    val storagePath = File(storageDir, "number_guess_appstate.json").absolutePath
    AppStatePersistence.startSync(
        storagePath = storagePath,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    )

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "NumberGuessGame",
        ) {
            App()
        }
    }
}
