package app.numberguessgame

import androidx.appstate.datastore.PersistToDataStore
import androidx.appstate.statestore.StateStoreKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

/**
 * Data model for persistent/shared game statistics.
 *
 * Annotated with [@Serializable] so that it can be automatically persisted to disk
 * via Jetpack AppState's DataStore integration.
 *
 * @property gamesWon Total number of games won across rounds.
 * @property bestScore The lowest attempt count achieved in any round (null if no game won yet).
 * @property totalAttempts Cumulative number of guesses made across all rounds.
 */
@Serializable
data class GameStats(
    val gamesWon: Int = 0,
    val bestScore: Int? = null,
    val totalAttempts: Int = 0
)

/**
 * A type-safe key used with Jetpack AppState's [androidx.appstate.statestore.StateStore].
 *
 * What is a StateStoreKey?
 * - In Jetpack AppState, state stored in a [androidx.appstate.statestore.StateStore] is accessed via typed keys.
 * - Each key defines its default value (`defaultValue = GameStats()`).
 *
 * What is [PersistToDataStore]?
 * - Implementing [PersistToDataStore] tells Jetpack AppState to automatically save and restore
 *   this key's value to/from disk using Jetpack DataStore whenever [androidx.appstate.datastore.syncToDataStore]
 *   is active.
 * - This ensures high scores and total games won survive process death and app restarts!
 */
@Serializable
data object GameStatsKey : StateStoreKey<GameStats>(
    defaultValue = GameStats()
), PersistToDataStore<GameStats> {
    override val name: String = "game_stats"
    override val serializer: KSerializer<GameStats> = GameStats.serializer()
}

