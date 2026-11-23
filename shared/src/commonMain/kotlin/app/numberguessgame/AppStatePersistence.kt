package app.numberguessgame

import androidx.appstate.datastore.syncToDataStore
import androidx.appstate.statestore.StateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Manages the application-wide [StateStore] and handles disk persistence via Jetpack DataStore.
 *
 * How it works:
 * 1. [stateStore] is a centralized, in-memory reactive container for application state.
 * 2. Calling [startSync] connects [stateStore] to a disk file path via [syncToDataStore].
 * 3. Any key implementing [androidx.appstate.datastore.PersistToDataStore] (such as [GameStatsKey])
 *    will automatically load its saved state on app launch and save every subsequent update to disk!
 * 4. This guarantees games won, best score, and stats survive app close, process death, and restarts.
 */
object AppStatePersistence {

    /**
     * The process-wide [StateStore] instance shared across screens and rounds.
     */
    val stateStore = StateStore()

    private var isSyncing = false

    /**
     * Connects the in-memory [stateStore] to a persistent file backed by Jetpack DataStore.
     *
     * @param storagePath Absolute file path where the JSON state should be saved.
     * @param scope A long-lived [CoroutineScope] (e.g. application or lifecycle scope) that
     *              manages the background read/write loop.
     */
    fun startSync(storagePath: String, scope: CoroutineScope) {
        if (isSyncing) return
        isSyncing = true

        scope.launch {
            try {
                stateStore.syncToDataStore(
                    path = storagePath,
                    coroutineScope = this
                )
            } catch (e: Exception) {
                println("AppStatePersistence: DataStore sync error: ${e.message}")
            }
        }
    }
}
