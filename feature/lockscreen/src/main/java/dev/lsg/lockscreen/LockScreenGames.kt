package dev.lsg.lockscreen

import dev.lsg.engine.GameCatalog

/**
 * The feature module does not know which games exist. The app wires the catalog in
 * (see Application.onCreate) so games can be added or removed without touching this module.
 */
object LockScreenGames {
    @Volatile
    lateinit var catalog: GameCatalog
}
