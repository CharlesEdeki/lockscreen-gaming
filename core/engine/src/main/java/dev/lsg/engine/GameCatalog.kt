package dev.lsg.engine

import kotlin.random.Random

class GameInfo(val id: String, val title: String, val create: () -> Game)

/** The set of games available to the lock screen. Adding a game = adding one entry here. */
class GameCatalog(val games: List<GameInfo>) {
    init {
        require(games.isNotEmpty()) { "catalog needs at least one game" }
        require(games.map { it.id }.toSet().size == games.size) { "duplicate game ids" }
    }

    /** Random pick that avoids repeating [excludeId] when there is any alternative. */
    fun pickRandom(random: Random = Random.Default, excludeId: String? = null): GameInfo {
        val pool = if (games.size > 1) games.filter { it.id != excludeId } else games
        return pool[random.nextInt(pool.size)]
    }
}
