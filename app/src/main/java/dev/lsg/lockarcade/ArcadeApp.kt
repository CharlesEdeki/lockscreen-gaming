package dev.lsg.lockarcade

import android.app.Application
import dev.lsg.engine.GameCatalog
import dev.lsg.games.bricks.BricksGame
import dev.lsg.games.dodge.DodgeGame
import dev.lsg.games.snake.SnakeGame
import dev.lsg.lockscreen.LockScreenGames

class ArcadeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // The one place that knows which games ship. Add a game module => add one line here.
        LockScreenGames.catalog = GameCatalog(
            listOf(SnakeGame.info, BricksGame.info, DodgeGame.info)
        )
    }
}
