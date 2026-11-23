package app.numberguessgame

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController {
    // Initialize Jetpack AppState DataStore persistence for iOS
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    val storagePath = (documentDirectory?.path ?: "") + "/number_guess_appstate.json"
    AppStatePersistence.startSync(
        storagePath = storagePath,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    )

    App()
}
